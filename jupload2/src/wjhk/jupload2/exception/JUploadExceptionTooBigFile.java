/**
 * This exception indicate the file that is to be upload is too big.
 * 
 * Note: the file to upload may be smaller than the file selected by the user. For instance, a picture may be reduced
 * before upload.
 * 
 */
package wjhk.jupload2.exception;

import wjhk.jupload2.policies.UploadPolicy;

public class JUploadExceptionTooBigFile extends JUploadException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param filename The filename for the file in error
	 * @param uploadLength The length of this file
	 * @param function The function in which the error occurs (nice, if the class name is given here, too)
	 * @param uploadPolicy The current upload policy.
	 */
	public JUploadExceptionTooBigFile(String filename, long uploadLength, String function, UploadPolicy uploadPolicy) {
		super(createErrorMessage(filename, uploadLength, function, uploadPolicy));
	}


	/**
	 * This method create the correct message for this 
	 * @param filename
	 * @param uploadLength
	 * @param function
	 */
	public static String createErrorMessage(String filename, long uploadLength, String function, UploadPolicy uploadPolicy) {
		return uploadPolicy.getString("errFileTooBig", filename, Long.toString(uploadLength));
	}
}