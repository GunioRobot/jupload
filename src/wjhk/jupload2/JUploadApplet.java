package wjhk.jupload2;

import java.applet.*;
import java.awt.*;

import wjhk.jupload2.gui.FilePanel;
import wjhk.jupload2.gui.JUploadPanel;
import wjhk.jupload2.gui.JUploadTextArea;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.policies.UploadPolicyFactory;

/**
 * The applet. It contains quite only the call to creation of the {@link wjhk.jupload2.gui.JUploadPanel},
 * which contains the real code.
 * <BR><BR>
 * The behaviour of the applet can easily be adapted, by :
 * <DIR>
 *   <LI> Using an existing {@link wjhk.jupload2.policies.UploadPolicy}, and specifying parameters.
 * 	 <LI> Creating a new upload policy, based on the {@link wjhk.jupload2.policies.DefaultUploadPolicy}, or created from scratch.
 * </DIR>
 * 
 * @author William JinHua Kwong (updated by Etienne Gauthier)
 */
public class JUploadApplet extends Applet{

/**
	 * 
	 */
	private static final long serialVersionUID = -3207851532114846776L;
//------------- INFORMATION --------------------------------------------
  public static final String TITLE = "JUpload JUploadApplet";
  public static final String DESCRIPTION =
      "Java Applet wrapper for JUploadPanel.";
  public static final String AUTHOR = "William JinHua Kwong (updated by Etienne Gauthier)";

  public static final String VERSION = "2.4.1rc1";
  public static final String LAST_MODIFIED = "21 d�c 2006";

  //----------------------------------------------------------------------

  //private boolean isStandalone = false;

  private UploadPolicy uploadPolicy;
  private JUploadPanel jUploadPanel;
  private JUploadTextArea statusArea; 

  //----------------------------------------------------------------------

  //Initialize the applet
  public void init() {
  	
	  try {
	    this.setLayout(new BorderLayout());    
	
	    //Creation of the Panel, containing all GUI objects for upload.
	    statusArea = new JUploadTextArea(5, 20);
	    uploadPolicy = UploadPolicyFactory.getUploadPolicy(this);

		jUploadPanel = new JUploadPanel(this, statusArea, uploadPolicy);
	
	    this.add(jUploadPanel, BorderLayout.CENTER);
	  } catch (Exception e) {
		  System.out.println(e.getMessage());
		  System.out.println(e.getStackTrace());		  
	  }

  }
  
  
  public FilePanel getFilePanel() {
	  return jUploadPanel.getFilePanel();
  }
  
	/**
	 * This allow runtime modifications of properties. Currently, this is only user after
	 * full initialization. This methods only calls the UploadPolicy.setProperty method. 
	 * 
	 * @param prop
	 * @param value 
	 */
	public void setProperty (String prop, String value) {
		try {
			jUploadPanel.getUploadPolicy().setProperty(prop, value);
		} catch (Exception e) {
			jUploadPanel.getUploadPolicy().displayErr("setProperty (exception " + e.getClass().getName() + ") : " + e.getMessage());
		}
	}


	/**
	 * @return the statusArea
	 */
	public JUploadTextArea getStatusArea() {
		return statusArea;
	}
 }
