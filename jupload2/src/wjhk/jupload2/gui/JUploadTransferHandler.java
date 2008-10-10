package wjhk.jupload2.gui;

/**
 * The JUploadTransferHandler allows easy management of pasted files onto the
 * applet. It just checks that the pasted selection is compatible (that is: it's
 * a file list), and calls the addFile methods, to let the core applet work.
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import wjhk.jupload2.exception.JUploadExceptionStopAddingFiles;
import wjhk.jupload2.policies.UploadPolicy;

class JUploadTransferHandler extends TransferHandler implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -1241261479500810699L;

    DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;

    /**
     * The JUpload panel for this applet.
     */
    JUploadPanel uploadPanel = null;

    /**
     * The current upload policy.
     */
    UploadPolicy uploadPolicy = null;

    /**
     * The standard constructor.
     * 
     * @param uploadPolicy
     */
    public JUploadTransferHandler(UploadPolicy uploadPolicy) {
        this.uploadPolicy = uploadPolicy;
        this.uploadPanel = this.uploadPolicy.getApplet().getUploadPanel();
    }

    /**
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
     *      java.awt.datatransfer.Transferable)
     */
    public boolean importData(JComponent c, Transferable t) {
        if (canImport(c, t.getTransferDataFlavors())) {
            try {
                List<File> fileList = (List<File>) t
                        .getTransferData(fileListFlavor);
                Iterator<File> iterator = fileList.iterator();
                while (iterator.hasNext()) {
                    this.uploadPanel.addFiles(iterator.next(), null);
                }
                return true;
            } catch (UnsupportedFlavorException ufe) {
                System.out.println("importData: unsupported data flavor");
            } catch (IOException ioe) {
                System.out.println("importData: I/O exception");
            } catch (JUploadExceptionStopAddingFiles e) {
                // Nothing to do: the user just cancel the adding of files,
                // because too many files are refused by the applet.
            }
        }
        return false;
    }

    /**
     * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
     */
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    /**
     * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
     *      java.awt.datatransfer.DataFlavor[])
     */
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (fileListFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String action = (String) e.getActionCommand();
        String a = action;
        action = a;
        /*
         * this.uploadPolicy.getApplet().getUploadPanel().getFilePanel().actionPerformed(new
         * ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
         *
        ((JUploadPanel)this.uploadPolicy.getApplet().getUploadPanel()).actionPerformed(new ActionEvent(this.uploadPolicy.getApplet().getUploadPanel(), ActionEvent.ACTION_PERFORMED,
                (String) e.getActionCommand()));*/
        //((FilePanelTableImp)this.uploadPolicy.getApplet().getUploadPanel().getFilePanel());
         
         
    }
}
