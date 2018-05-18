#!/bin/bash

cd ~ &&
echo ===================================
echo = Installing Java SDK and Git     =   
echo ===================================
sudo yum -y install java-1.7.0-devel git &&
echo ===================================
echo = Fetching and unzip AWS SDK      =   
echo ===================================
wget http://sdk-for-java.amazonwebservices.com/latest/aws-java-sdk.zip &&
unzip aws-java-sdk.zip &&
rm aws-java-sdk.zip &&
echo ===================================
echo = Cloning Git Repo                =   
echo ===================================
git clone https://github.com/Ahlid/CNV_PARA_O_20.git &&
echo ===================================
echo = Compiling project               =   
echo ===================================
AWS_SDK_DIR=$(echo aws-java-sdk-*)
AWS_SDK_VERSION=${AWS_SDK_DIR#aws-java-sdk-}
sed -i "s/AWS_VERSION=.*/AWS_VERSION=$AWS_SDK_VERSION/" ~/CNV_PARA_O_20/Makefile &&
cd CNV_PARA_O_20/ && make all && make run_inst && cd .. &&
echo ===================================
echo = Creating AWS Credentials        =   
echo ===================================
echo "Access Key:"
read key
echo "Secret Access Key:"
read secret
mkdir ~/.aws
echo [default] >> ~/.aws/credentials
echo aws_access_key_id=$key >> ~/.aws/credentials
echo aws_secret_access_key=$secret >> ~/.aws/credentials
echo [default] >> ~/.aws/config
echo region=us-east-1 >> ~/.aws/config
echo output=text >> ~/.aws/config
echo =====================================
echo = Creating worker and balancer AMIs =   
echo =====================================
INSTANCE_ID=$(ec2-metadata --instance-id | cut -d ' ' -f2)
echo "Creating AMI: worker-ami"
sudo cp ~/CNV_PARA_O_20/scripts/rc.local.worker /etc/rc.local
WORKER_AMI_ID=$(aws ec2 create-image --instance-id $INSTANCE_ID --no-reboot --name worker-ami)
aws ec2 wait image-available --image-ids $WORKER_AMI_ID
echo "Worker AMI Id: $WORKER_AMI_ID"

echo "Creating AMI: balancer-ami"
sudo cp ~/CNV_PARA_O_20/scripts/rc.local.balancer /etc/rc.local
BALANCER_AMI_ID=$(aws ec2 create-image --instance-id $INSTANCE_ID --no-reboot --name balancer-ami)
aws ec2 wait image-available --image-ids $BALANCER_AMI_ID
echo "Balancer AMI Id: $BALANCER_AMI_ID"

echo "Updating Worker AMI on DynamoDB"
cd CNV_PARA_O_20/ && make updateami name=$WORKER_AMI_ID && cd .. &&

echo Done
