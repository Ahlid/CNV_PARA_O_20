cd ~ &&
echo ===================================
echo = Installing java sdk and git     =
echo ===================================
sudo yum install java-1.7.0-devel git &&
echo ===================================
echo = Fetching and unzip AWS SDK      =
echo ===================================
wget http://sdk-for-java.amazonwebservices.com/latest/aws-java-sdk.zip &&
unzip aws-java-sdk.zip &&
echo ===================================
echo = Clone Git Repo                  =
echo ===================================
git clone https://github.com/Ahlid/CNV_PARA_O_20.git &&
echo ===================================
echo = Create Aws Credentials          =
echo ===================================
echo "Access Key:"
read key
echo "Secret Access Key:"
read secret
mkdir ~/.aws
echo [default] >> ~/.aws/credentials
echo aws_access_key_id=$key >> ~/.aws/credentials
echo aws_secret_access_key=$secret >> ~/.aws/credentials

echo ===================================
echo = Copying autostart item          =
echo ===================================
sudo cp ~/CNV_PARA_O_20/scripts/rc.local /etc/rc.local
