#!/bin/bash

INSTANCE_ID=$(ec2-metadata --instance-id | cut -d ' ' -f2) &&

# Deregister AMI with the name CNV-worker-ami if it already exists
OLD_WORKER_AMI_ID=$(aws ec2 describe-images --owners self --filters Name=name,Values=CNV-worker-ami | sed -n 's/\s*"ImageId": "\(.*\)",/\1/gp') &&
if [ $OLD_WORKER_AMI_ID ]; then
  echo "Deregistering previously created AMI: CNV-worker-ami"
  aws ec2 deregister-image --image-id $OLD_WORKER_AMI_ID
fi

# Create worker AMI and wait until it is available
echo "Creating new AMI: CNV-worker-ami"
WORKER_AMI_ID=$(aws ec2 create-image --instance-id $INSTANCE_ID --no-reboot --name CNV-worker-ami | grep -o "ami-[a-zA-Z0-9]*") &&
aws ec2 wait image-available --image-ids $WORKER_AMI_ID &&
echo "Worker AMI Id: $WORKER_AMI_ID"

echo "Updating Worker AMI on DynamoDB"
cd ~/CNV_PARA_O_20/ && make updateami name=$WORKER_AMI_ID &> /dev/null && cd ..
