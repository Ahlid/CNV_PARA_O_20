#!/bin/bash

cd ~ &&
INSTANCE_ID=$(ec2-metadata --instance-id | cut -d ' ' -f2) &&

echo "Recreating AMI: worker-ami"
OLD_WORKER_AMI_ID=$(aws ec2 describe-images --owners self --filters Name=name,Values=worker-ami | sed -n 's/\s*"ImageId": "\(.*\)",/\1/gp')
[ -n $OLD_WORKER_AMI_ID ] && aws ec2 deregister-image --image-id $OLD_WORKER_AMI_ID

WORKER_AMI_ID=$(aws ec2 create-image --instance-id $INSTANCE_ID --no-reboot --name worker-ami) &&
aws ec2 wait image-available --image-ids $WORKER_AMI_ID &&
echo "Worker AMI Id: $WORKER_AMI_ID"

echo "Updating Worker AMI on DynamoDB"
cd CNV_PARA_O_20/ && make updateami name=$WORKER_AMI_ID &> /dev/null && cd .. &&
echo Done
