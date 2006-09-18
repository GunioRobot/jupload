package wjhk.jupload2.upload;

/**
 * Current version of FileUploadThread: uses the {@link UploadPolicy} interface.
 * 
 * This class is a copy of the original FileUploadThreadV2 : it uses an array of
 * FileData, instead of an array of Files. This allow any default initialization or
 * work on the file, before upload. For instance : resize a picture, check an xml,..
 * 
 * This class should now be easily resusable, as it uses the FileData.getInputStream
 * to read what ever result of these transformation ... or the file data, if no
 * transformation.
 * 
 * <HR>
 * <B>Orginal FileUploadThreadV2 comment :</B><BR> 
 * 
 * URLConnection instance given by the URL class openConnection() function
 * can't handle uploading of large files.
 *
 * The reason being? URLConnection only does a post to the server after the
 * getInputStream() function is called. So anything you write to the Output
 * Stream before the getInputStream() is called will be written to memory.
 * For large files this will caused the JVM to throw an Out of Memory exception.
 *
 * With the above reason I have decided to replace the use of URLConnection
 * with sockets.
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import javax.swing.JProgressBar;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

public class FileUploadThreadV3 extends Thread {
	
	//------------- INFORMATION --------------------------------------------
	public static final String TITLE = "JUpload FileUploadThreadV3";
	public static final String DESCRIPTION =
		"Java Thread to upload files into a web server (based ont FileUploadThreadV2).";
	public static final String AUTHOR = "Etienne Gauthier";
	
	public static final double VERSION = 2.2;
	public static final String LAST_MODIFIED = "20 may 2006";
	
	//------------- CONSTANTS ----------------------------------------------
	
	/**
	 * MAX_WAIT is the longuer time that the thread should wait for pictures to 
	 * be ready.
	 */
	public static final long MAX_WAIT = 20 * 1000;
	
	
	//------------- VARIABLES ----------------------------------------------
	/**
	 * Files asked to be uploaded into the server.
	 */ 
	private FileData[] allFiles;
	
	/**
	 * The upload policy contains all parameters needed to define the way files should be uploaded, including the URL. 
	 */
	private UploadPolicy uploadPolicy;
	
	// Progress Bar.
	private JProgressBar progress;
	private long totalFilesLength;
	private long uploadedLength;
	
	// Stopping the thread.
	private boolean stop = false;
	
	// Server Output.
	private StringBuffer sbServerOutput = new StringBuffer();
	
	// Thread Exception.
	private Exception e = null;
	
	//------------- CONSTRUCTOR --------------------------------------------
	public FileUploadThreadV3(FileData[] allFiles, UploadPolicy uploadPolicy){
		this.allFiles = allFiles;
		this.uploadPolicy = uploadPolicy;
		
		totalFilesLength = 0;
	}
	
	//------------- Public Functions ---------------------------------------
	
	// Setting Progress Panel.
	public void setProgressPanel(JProgressBar pgrBar){
		progress = pgrBar;
	}
	
	// Stopping the Thread
	public void stopUpload(){
		this.stop = true;
	}
	
	// Server Output.
	public StringBuffer getServerOutput(){
		return sbServerOutput;
	}
	
	// Exceptions
	public Exception getException(){
		return e;
	}
	
	//------------- Private Functions --------------------------------------
	private StringBuffer getRandomString(){
		StringBuffer sbRan = new StringBuffer(11);
		StringBuffer alphaNum= new StringBuffer();
		alphaNum.append("1234567890abcdefghijklmnopqrstuvwxyz");
		int num;
		for(int i = 0; i < 11; i++){
			num = (int)(Math.random()* (alphaNum.length() - 1));
			sbRan.append(alphaNum.charAt(num));
		}
		return sbRan;
	}
	
	private void uploadFileStream(InputStream is, DataOutputStream dOut)
	throws FileNotFoundException, IOException {
		byte[] byteBuff = null;
		try{
			int numBytes = 0;
			byteBuff = new byte[1024];
			while(-1 != (numBytes = is.read(byteBuff)) && !stop){
				dOut.write(byteBuff, 0, numBytes);
				uploadedLength += numBytes;
				if(null != progress) progress.setValue((int)uploadedLength);
			}
		}finally{
			try{
				is.close();
			}catch(Exception e){}
			byteBuff = null;
		}
	}
	
	private void clearServerOutPut(){
		sbServerOutput.setLength(0);
		sbServerOutput.append("\n");
	}
	
	private void addServerOutPut(String s){
		if(0 < sbServerOutput.length() || !s.equals("")){
			sbServerOutput.append(s);
		}
	}
	
	private StringBuffer[] setAllHead(FileData[] fileA, StringBuffer bound) throws JUploadException {
		StringBuffer[] sbArray = new StringBuffer[fileA.length];
		FileData fileData;
		StringBuffer sb;
		String fileName;
		for(int i=0; i < fileA.length; i++){
			fileData = fileA[i];
			sbArray[i] = new StringBuffer();
			sb = sbArray[i];
			// Line 1.
			sb.append(bound.toString());sb.append("\r\n");
			// Line 2.
			fileName = uploadPolicy.getUploadFilename(fileData, i);
			uploadPolicy.displayDebug(uploadPolicy.getString("uploadedFileName") + ": " + fileName, 70);
			sb.append("Content-Disposition: form-data; name=\"")
			.append(fileName)
			.append("\"; filename=\"")
			.append(fileData.getFileName())
			.append("\"")
			.append("\r\n");
			// Line 3 & Empty Line 4.
			sb	.append("Content-Type: ")
			.append(fileData.getMimeType())
			.append("\r\n");
			sb.append("\r\n");
			uploadPolicy.displayDebug("head : '" +  sb.toString() + "'", 70);
		}
		return sbArray;
	}
	
	private StringBuffer[] setAllTail(int fileLength, StringBuffer bound){
		StringBuffer[] sbArray = new StringBuffer[fileLength];
		for(int i=0; i < fileLength; i++){
			sbArray[i] = new StringBuffer("\r\n");
		}
		// Telling the Server we have Finished.
		sbArray[sbArray.length-1].append(bound.toString());
		sbArray[sbArray.length-1].append("--\r\n");
		return sbArray;
	}
	//------------- THE HEART OF THE PROGRAM ------------------------------
	
	private void setHeader(){
	}
	
	
	public void run() {
		boolean bUploadOk = true;
		uploadedLength = 0;
		
		if (!prepareUpload()) {
			e = new Exception(uploadPolicy.getString("errUploadNotReady"));
		} else {
			try {
				if(null != progress)  {
					progress.setValue(0);
					progress.setMaximum(this.allFiles.length);
				}
				for(int i=0; i < this.allFiles.length; i++){
					if(null != progress)  {
						progress.setValue(i);
						progress.setString(uploadPolicy.getString("preparingFile", (i+1) + "/" + (this.allFiles.length) ));
					}
					totalFilesLength += this.allFiles[i].getUploadLength();
				}
				
				if(null != progress)  {
					progress.setValue(0);
					progress.setMaximum((int)totalFilesLength);
					progress.setString("");
				}
				//Prepare upload
				//Let's take the upload policy into accound  : how many files at a time ?
				int nbMaxFilesPerUpload = uploadPolicy.getMaxFilesPerUpload();
				FileData[] filesToUpload;
				if (nbMaxFilesPerUpload <= 0) {
					nbMaxFilesPerUpload = Integer.MAX_VALUE;
					filesToUpload = new FileData[allFiles.length];
				} else {
					filesToUpload = new FileData[nbMaxFilesPerUpload];
				}
				
				//We upload files, according to the current upload policy.
				int iPerUploadCount = 0;
				int iTotalFileCount = 0;
				while (iTotalFileCount < allFiles.length  &&  bUploadOk) {
					//Wait for the current file to bee ready.
					long wait = 100;
					long totalWait = 0;
					while (! allFiles[iTotalFileCount].isUploadReady()) {
						try {
							sleep(wait);
						} catch (InterruptedException e) {
							//Let's go on ...
						}
						totalWait += wait;
						if (totalWait > MAX_WAIT) {
							throw new JUploadException("FileUploadThreadV3.run : " + uploadPolicy.getString("errWaitTooLong"));
						}
					}
					filesToUpload[iPerUploadCount] = allFiles[iTotalFileCount]; 
					iPerUploadCount += 1;
					iTotalFileCount += 1;
					if (iPerUploadCount == nbMaxFilesPerUpload) {
						//Let's do an upload.
						bUploadOk = doUpload (filesToUpload, iPerUploadCount, iTotalFileCount);
						iPerUploadCount = 0;
					}
				}//while
				
				if (iPerUploadCount > 0  &&  bUploadOk) {
					//Some files are still to upload. Let's finish the job.
					bUploadOk = doUpload (filesToUpload, iPerUploadCount, iTotalFileCount);
				}

				//Let's show everything is Ok
				if(null != progress)  {
					if (bUploadOk) {
						progress.setString(uploadPolicy.getString("nbUploadedFiles", iTotalFileCount));
					} else {
						progress.setString("errDuringUpload");
					}
				}
			} catch (Exception e) {
				uploadPolicy.displayErr(e);
				progress.setString(e.getMessage());
			} finally {
				//In all cases, we try to free all reserved resources.
				for(int i=0; i < allFiles.length; i++){
					allFiles[i].afterUpload();
				}
			}
		}
		
		//If the upload was unsuccessful, we try to alert the webmaster.
		if (!bUploadOk) {
			uploadPolicy.sendDebugInformation("Error in Upload");
		}
		
	}//run
	
	/**
	 * Actually do file upload. It's called by the run methods, once for all files, or file by file, 
	 * depending on the UploadPolicy.
	 * 
	 * @param filesA An array of FileData, that contains all files to upload in this HTTP request.
	 * @param nbFilesToUpload The number of files in filesA to use (indice 0 to nbFilesToUpload-1).
	 * @param iTotalFileCount The total number of files that are to upload. It is used to generate the "file 1 out of 4 " message, on the progress bar. 
	 *
	 */
	private boolean doUpload (FileData[] filesA, int nbFilesToUpload, int iTotalFileCount) {
		boolean bReturn = true;
		Socket sock = null;
		DataOutputStream dataout = null;
		BufferedReader datain = null;
		String msg;
		String action = "init";
		StringBuffer header = new StringBuffer();
		
		clearServerOutPut();
		
		if (nbFilesToUpload == 1) {
			msg = iTotalFileCount + "/" + allFiles.length;
		} else {
			msg = (iTotalFileCount - nbFilesToUpload + 1) + "-" + iTotalFileCount + "/" + allFiles.length;
		}
		
		if(!stop && (null != progress))
			progress.setString(uploadPolicy.getString("infoUploading", msg));
		
		try{
			action = "get URL";
			URL url = new URL(uploadPolicy.getPostURL());
			
			StringBuffer boundary = new StringBuffer();
			boundary.append("-----------------------------");
			boundary.append(getRandomString().toString());
			
			StringBuffer[] head = setAllHead(filesA, boundary);
			StringBuffer[] tail = setAllTail(nbFilesToUpload, boundary);
			
			long contentLength = 0;
			for(int i = 0; i < nbFilesToUpload; i++){
				contentLength += head[i].length();
				contentLength += filesA[i].getUploadLength();
				contentLength += tail[i].length();
			}
			
			// Header: Request line
			action = "append headers";
			header.append("POST ");header.append(url.getPath());
			if(null != url.getQuery() && !"".equals(url.getQuery())){
				header.append("?");header.append(url.getQuery());
			}
			header.append(" ").append(uploadPolicy.getServerProtocol()).append("\r\n");
			// Header: General
			header.append("Host: ");
			header.append(url.getHost());header.append("\r\n");
			header.append("Accept: */*\r\n");
			header.append("Content-type: multipart/form-data; boundary=");
			header.append(boundary.substring(2, boundary.length()) +"\r\n");
			header.append("Connection: close\r\n");
			header.append("Content-length: ")
				  .append(contentLength-2)
				  .append("\r\n");
			
			//Get specific headers for this upload.
			uploadPolicy.onAppendHeader(header);
			
			// Blank line (end of header)
			header.append("\r\n");
			
			// If port not specified then use default http port 80.
			sock = new Socket(url.getHost(), (-1 == url.getPort())?80:url.getPort());
			dataout = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
			datain  = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//DataInputStream datain  = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
			
			// Send http request to server
			action = "send bytes (1)";
			dataout.writeBytes(header.toString());
			for(int i=0; i < nbFilesToUpload && !stop; i++){
				// Write to Server the head(4 Lines), a File and the tail.
				action = "send bytes (20)" + i;
				dataout.writeBytes(head[i].toString());
				action = "send bytes (30)" + i;
				uploadFileStream(filesA[i].getInputStream(),dataout);
				action = "send bytes (40)" + i;
				dataout.writeBytes(tail[i].toString());
			}
			action = "flush";
			dataout.flush ();
			if(!stop && (null != progress))
				progress.setString(uploadPolicy.getString("infoUploaded", msg));
			
			
			action = "wait for server answer";
			String strUploadSuccess = uploadPolicy.getStringUploadSuccess();
			boolean uploadSuccess = false;
			boolean readingHttpBody = false;
			StringBuffer sbHttpResponseBody = new StringBuffer(); 
			String line;
			/*
			 * Below is a trace of my tests to correct upload: upload under HTTP 1.1 generates a quite long wait time
			 * after each HTTP upload request. It has actually be corrected by the '-2' in the content-length.
			 * See above.
			 * I keep this part of code, to make it easier to do new tests, if necessary.
			 *  
			switch (1)
			{
			case 1:
			*/
				//Now, we wait for the full answer (which should mean that the uploaded files
				//has been treated on the server)
				while ((line = datain.readLine()) != null) {
					this.addServerOutPut(line);
					this.addServerOutPut("\n");
					
					//Is this upload a success ?
					action ="test success";
					if (line.matches(strUploadSuccess)) {
						uploadSuccess = true;
					}
					
					//Store the http body 
					if (readingHttpBody) {
						action = "sbHttpResponseBody";
						sbHttpResponseBody.append(line).append("\n");
					}
					if (line.length() == 0) {
						//Next lines will be the http body (or perhaps we already are in the body, but it's Ok anyway) 
						action = "readingHttpBody";
						readingHttpBody = true;
					}
				}
			/*
				break;
			case 2:
				//We first close the connexion, to make http 1.1 answer quicker.
				dataout.close();
				break;
			case 3:
				//We first close the connexion, to make http 1.1 answer quicker.
				dataout.close();
				//Now, we wait for the full answer (which should mean that the uploaded files
				//has been treated on the server)
				while ((line = datain.readLine()) != null) {
					this.addServerOutPut(line + "\n");
				}
				break;
			case 4: //KO
				//Let's write some blank lines, to indicate that our request is finished.
				dataout.writeChars("\n");
				dataout.writeChars("\n");
				//Now, we wait for the full answer (which should mean that the uploaded files
				//has been treated on the server)
				while ((line = datain.readLine()) != null) {
					this.addServerOutPut(line + "\n");
				}
				break;
			case 5:
				//Let's write some blank lines, to indicate that our request is finished.
				dataout.write(0);
				dataout.write(0);
				//Now, we wait for the full answer (which should mean that the uploaded files
				//has been treated on the server)
				while ((line = datain.readLine()) != null) {
					this.addServerOutPut(line + "\n");
				}
				break;
			}
			*/
				
			//Is our upload a success ?
			if (! uploadSuccess) {
				throw new JUploadExceptionUploadFailed(uploadPolicy.getString("errHttpResponse"));
			}

		}catch(Exception e){
			this.e = e;
			bReturn = false;
			uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload") + " (main | " + action + ") (" + e.getClass() + ") : " + e.getMessage());
		}finally{
			try{
				// Throws java.io.IOException
				dataout.close();
			} catch(Exception e) {
				this.e = e;
				bReturn = false;
				uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload") + " (dataout.close) (" + e.getClass() + ") : " + e.getMessage());
			}
			dataout = null;
			try{
				// Throws java.io.IOException
				datain.close();
			} catch(Exception e){}
			datain = null;
			try{
				// Throws java.io.IOException
				sock.close();
			} catch(Exception e) {
				bReturn = false;
				uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload") + " (sock.close)(" + e.getClass() + ") : " + e.getMessage());
			}
			sock = null;
			uploadPolicy.displayDebug ("Sent to server : " + header.toString(), 40);
			uploadPolicy.displayDebug ("Serveur output : " + getServerOutput().toString(), 10);
		}
		
		return bReturn;
	}
	
	/**
	 * This method create a thread that will call FileData.beforeUpload for each fileData
	 * to upload.
	 */
	private boolean prepareUpload () {
		if (!uploadPolicy.isUploadReady()) {
			return false;
		}
		new  Thread(){ 
			public void run() {
				int i = 0;
				while (i < allFiles.length) {
					allFiles[i++].beforeUpload();
				}
			}//run
		}.start();
		
		return true;
	}
	//------------- CLEAN UP -----------------------------------------------
	public void close(){
		allFiles = null;
		e = null;
		sbServerOutput = null;
	}
}
