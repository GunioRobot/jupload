//
// $Id$
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: 2006-07-11
// Creator: Etienne Gauthier
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version. This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details. You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation, Inc.,
// 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.gui;

import java.awt.event.MouseEvent;

import wjhk.jupload2.filedata.PictureFileData;
import wjhk.jupload2.policies.UploadPolicy;


/**
 * 
 * The picture for the PictureDialog. The difference with the PicturePanel, is that
 * a click on it closes the Dialog. 
 * 
 * @author Etienne Gauthier
 */
public class DialogPicturePanel extends PicturePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1333603128496671158L;
	/**
	 * The JDialog containing this panel.
	 */
	PictureDialog pictureDialog;
	
	/**
	 * 
	 */
	public DialogPicturePanel(PictureDialog pictureDialog, UploadPolicy uploadPolicy, PictureFileData pictureFileData) {
		super(pictureDialog.getContentPane(), false, uploadPolicy);
		
		this.pictureDialog = pictureDialog;
		setPictureFile(pictureFileData);
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
    public void mouseClicked(@SuppressWarnings("unused")
    MouseEvent arg0) {
		//Let's close the current DialogBox, if it has not already be done.
		if (this.pictureDialog != null) {
			this.uploadPolicy.displayDebug("[DialogPicturePanel] Before pictureDialog.dispose()", 60);
			this.pictureDialog.dispose();
			this.pictureDialog = null;
		}
	}
}
