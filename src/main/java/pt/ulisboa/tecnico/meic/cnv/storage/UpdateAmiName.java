package pt.ulisboa.tecnico.meic.cnv.storage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

public class UpdateAmiName {

    public static void main(String[] args) throws Exception {
        Messenger m = null;
        int res = 0;
        String result = null;

        try {
            m = Messenger.getInstance();
            m.setup();
            if (args.length >= 1) {
                System.out.println("Changing Worker AMI to: " + args[0]);
                res = m.changeAmiName(args[0]);
                System.out.println("Result: " + res);
            } else {
                result = m.getAMIName();
                System.out.println("Current AMI is: " + result);
            }

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
