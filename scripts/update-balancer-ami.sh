#!/bin/bash

cd ~ &&
INSTANCE_ID=$(ec2-metadata --instance-id | cut -d ' ' -f2) &&

echo "Recreating AMI: balancer-ami"
OLD_BALANCER_AMI_ID=$(aws ec2 describe-images --owners self --filters Name=name,Values=balancer-ami | sed -n 's/\s*"ImageId": "\(.*\)",/\1/gp')
[ -n $OLD_BALANCER_AMI_ID ] && aws ec2 deregister-image --image-id $OLD_BALANCER_AMI_ID

BALANCER_AMI_ID=$(aws ec2 create-image --instance-id $INSTANCE_ID --no-reboot --name balancer-ami) &&
aws ec2 wait image-available --image-ids $BALANCER_AMI_ID &&
echo "Balancer AMI Id: $BALANCER_AMI_ID"

echo Done
