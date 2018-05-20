#!/bin/bash

delete_sg_if_exists() {
  if [ ! $1 ]; then
    echo "Please indicate a Security Group name"
    exit
  fi 
  GROUP_NAME=$1
  SG_EXISTS=$(aws ec2 describe-security-groups --filters Name=group-name,Values=$GROUP_NAME | grep "GroupName") &&
  if [ "$SG_EXISTS" ]; then
    # Terminating instances which use a SG is a prerequisite for deleting that SG
    # Retrieve all instance ids which use this Security Group
    INSTANCES_TERMINATE=$(aws ec2 describe-instances --filters Name=instance.group-name,Values=$GROUP_NAME | sed -n 's/\s*"InstanceId": "\(.*\)",/\1/gp' | tr '\n' ' ') &&
    if [ "$INSTANCES_TERMINATE" ]; then
      echo "The following instances use the Security Group $GROUP_NAME: $INSTANCES_TERMINATE"
      echo "Terminating these instances in order to be able to delete the Security Group"
      aws ec2 terminate-instances --instance-ids $INSTANCES_TERMINATE &> /dev/null &&
      aws ec2 wait instance-terminated --instance-ids $INSTANCES_TERMINATE
    fi
    # Delete security group if it previously existed
    echo "Deleting previously created Security Group: $GROUP_NAME"
    aws ec2 delete-security-group --group-name $GROUP_NAME &> /dev/null
  fi
}

cd ~ &&
echo =======================================
echo = Installing Java SDK and Git         =
echo =======================================
sudo yum -y install java-1.7.0-devel git &&
echo =======================================
echo = Fetching and unzipping AWS SDK      =
echo =======================================
wget http://sdk-for-java.amazonwebservices.com/latest/aws-java-sdk.zip &&
unzip aws-java-sdk.zip &&
rm aws-java-sdk.zip &&
echo =======================================
echo = Cloning Git Repo                    =
echo =======================================
git clone https://github.com/Ahlid/CNV_PARA_O_20.git &&
echo =======================================
echo = Compiling project                   =
echo =======================================
AWS_SDK_DIR=$(echo aws-java-sdk-*)
AWS_SDK_VERSION=${AWS_SDK_DIR#aws-java-sdk-}
sed -i "s/AWS_VERSION=.*/AWS_VERSION=$AWS_SDK_VERSION/" ~/CNV_PARA_O_20/Makefile &&
cd CNV_PARA_O_20/ && make all && cd .. &&
echo =======================================
echo = Creating AWS Credentials            =
echo =======================================
echo "Access Key:"
read key &&
echo "Secret Access Key:"
read secret &&
mkdir ~/.aws &&
echo [default] >> ~/.aws/credentials &&
echo aws_access_key_id=$key >> ~/.aws/credentials &&
echo aws_secret_access_key=$secret >> ~/.aws/credentials &&
echo [default] >> ~/.aws/config &&
echo region=us-east-1 >> ~/.aws/config &&
echo output=json >> ~/.aws/config &&
echo =======================================
echo = Creating worker and balancer AMIs   =
echo =======================================
# Copy rc.local of worker to /etc/rc.local
sudo cp ~/CNV_PARA_O_20/scripts/rc.local.worker /etc/rc.local && sync &&
source ~/CNV_PARA_O_20/scripts/update-worker-ami.sh &&
# Copy rc.local of balancer to /etc/rc.local
sudo cp ~/CNV_PARA_O_20/scripts/rc.local.balancer /etc/rc.local && sync &&
source ~/CNV_PARA_O_20/scripts/update-balancer-ami.sh &&
echo =======================================
echo = Creating worker and balancer SGs    =
echo =======================================
# Security Group for workers
delete_sg_if_exists CNV-worker-sg
echo "Creating new Security Group: CNV-worker-sg"
aws ec2 create-security-group --description "Allows SSH + HTTP at a worker instance" --group-name CNV-worker-sg &> /dev/null &&
aws ec2 authorize-security-group-ingress --group-name CNV-worker-sg --protocol tcp --port 8000 --cidr 0.0.0.0/0 &&
aws ec2 authorize-security-group-ingress --group-name CNV-worker-sg --protocol tcp --port 22 --cidr 0.0.0.0/0 &&
# Security Group for the balancer
delete_sg_if_exists CNV-balancer-sg
echo "Creating new Security Group: CNV-balancer-sg"
aws ec2 create-security-group --description "Allows SSH + HTTP at the load balancer instance" --group-name CNV-balancer-sg &> /dev/null &&
aws ec2 authorize-security-group-ingress --group-name CNV-balancer-sg --protocol tcp --port 80 --cidr 0.0.0.0/0 &&
aws ec2 authorize-security-group-ingress --group-name CNV-balancer-sg --protocol tcp --port 22 --cidr 0.0.0.0/0 &&
echo =======================================
echo = Launching a load balancer instance  =
echo =======================================
KEY_NAMES=$(aws ec2 describe-key-pairs | sed -n 's/\s*"KeyName": "\(.*\)",/\1/gp' | tr '\n' ' ') &&
echo "Choose a valid key pair for logging in to the balancer"
echo "Available key pairs: $KEY_NAMES"
read key_name &&
aws ec2 run-instances --image-id $BALANCER_AMI_ID --count 1 --instance-type t2.micro --security-groups CNV-balancer-sg --key-name $key_name &> /dev/null &&

echo Done
echo "You may stop the load balancer instance anytime to stop incurring costs"
echo "Terminating current instance as it is no longer needed"
aws ec2 terminate-instances --instance-ids $INSTANCE_ID &> /dev/null
