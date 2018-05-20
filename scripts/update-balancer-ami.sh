#!/bin/bash

INSTANCE_ID=$(ec2-metadata --instance-id | cut -d ' ' -f2) &&

# Deregister AMI with the name CNV-balancer-ami if it already exists
OLD_BALANCER_AMI_ID=$(aws ec2 describe-images --owners self --filters Name=name,Values=CNV-balancer-ami | sed -n 's/\s*"ImageId": "\(.*\)",/\1/gp') &&
if [ $OLD_BALANCER_AMI_ID ]; then
  echo "Deregistering previously created AMI: CNV-balancer-ami"
  aws ec2 deregister-image --image-id $OLD_BALANCER_AMI_ID
fi

# Create balancer AMI and wait until it is available
echo "Creating new AMI: CNV-balancer-ami"
BALANCER_AMI_ID=$(aws ec2 create-image --instance-id $INSTANCE_ID --no-reboot --name CNV-balancer-ami | grep -o "ami-[a-zA-Z0-9]*") &&
aws ec2 wait image-available --image-ids $BALANCER_AMI_ID &&
echo "Balancer AMI Id: $BALANCER_AMI_ID"
