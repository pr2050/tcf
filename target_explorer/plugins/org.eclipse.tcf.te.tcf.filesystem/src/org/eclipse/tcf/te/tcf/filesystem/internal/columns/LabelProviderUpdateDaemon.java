/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.columns;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * The background daemon that updates the images of the file system using
 * images retrieved by FileSystemView from Swing.
 */
public abstract class LabelProviderUpdateDaemon extends Thread {
	// The dummy AWT component used to render the icon.
	Component dummyComponent = new JComponent(){private static final long serialVersionUID = 5926798769323111209L;};
	//The queue that caches the current file nodes to be updated.
	BlockingQueue<FSTreeNode> queueNodes;
	

	/**
	 * Constructor
	 */
	public LabelProviderUpdateDaemon() {
		super("Label Provider Updater"); //$NON-NLS-1$
		setDaemon(true);
		this.queueNodes = new LinkedBlockingQueue<FSTreeNode>();
	}

	/**
	 * Cache the node which is to be updated with its icon in the file tree.
	 * 
	 * @param node The node to be enqueued
	 */
	public void enqueue(final FSTreeNode node) {
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void handleException(Throwable exception) {
				// Ignore
			}

			@Override
			public void run() throws Exception {
				queueNodes.put(node);
			}
		});
	}

	/**
	 * Take next node to be processed.
	 * 
	 * @return The next node.
	 */
	private FSTreeNode take() {
		while (true) {
			try {
				return queueNodes.take();
			}
			catch (InterruptedException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			FSTreeNode node = take();
			String key = getImageKey(node);
			ImageDescriptor image = UIPlugin.getImageDescriptor(key);
			if (image == null) {
				File mirror = getMirrorFile(node);
				File imgFile = getImgFile(mirror.getName());
				if (!imgFile.exists()) {
					FileSystemView view = FileSystemView.getFileSystemView();
					Icon icon = view.getSystemIcon(mirror);
					createImageFromIcon(icon, mirror.getName());
				}
				try {
					image = ImageDescriptor.createFromURL(imgFile.toURI().toURL());
					UIPlugin.getDefault().getImageRegistry().put(key, image);
					sendNotification(node, node.name, null, image);
				}
				catch (MalformedURLException e) {
					// Ignore
				}
			}
		}
	}
	
	/**
	 * Get the image of disk drivers on Windows platform.
	 * 
	 * @return The disk driver image.
	 */
	public Image getDiskImage() {
		String key = "SWING_ROOT_DRIVER_IMAGE"; //$NON-NLS-1$
		ImageDescriptor imgDesc = UIPlugin.getImageDescriptor(key);
		if(imgDesc == null) {
			File mirror = new File("C:\\"); //$NON-NLS-1$
			File imgFile = getImgFile("_root_driver_"); //$NON-NLS-1$
			if(!imgFile.exists()) {
				FileSystemView view = FileSystemView.getFileSystemView();
				Icon icon = view.getSystemIcon(mirror);
				createImageFromIcon(icon, "_root_driver_"); //$NON-NLS-1$
			}
			try {
				imgDesc = ImageDescriptor.createFromURL(imgFile.toURI().toURL());
				UIPlugin.getDefault().getImageRegistry().put(key, imgDesc);
			}
			catch (MalformedURLException e) {
				// Ignore
			}
		}
		return UIPlugin.getImage(key);
	}
	
	/**
	 * Get the folder image on Windows platform.
	 * 
	 * @return The folder image.
	 */
	public Image getFolderImage() {
		String key = "SWING_FOLDER_IMAGE"; //$NON-NLS-1$
		ImageDescriptor imgDesc = UIPlugin.getImageDescriptor(key);
		if(imgDesc == null) {
			File mirror = getImgDir();
			File imgFile = getImgFile("_directory_"); //$NON-NLS-1$
			if(!imgFile.exists()) {
				FileSystemView view = FileSystemView.getFileSystemView();
				Icon icon = view.getSystemIcon(mirror);
				createImageFromIcon(icon, "_directory_"); //$NON-NLS-1$
			}
			try {
				imgDesc = ImageDescriptor.createFromURL(imgFile.toURI().toURL());
				UIPlugin.getDefault().getImageRegistry().put(key, imgDesc);
			}
			catch (MalformedURLException e) {
				// Ignore
			}
		}
		return UIPlugin.getImage(key);
	}
	
	/**
	 * Get an extension key as the image registry key for the 
	 * specified node.
	 * 
	 * @param node The node to get the key for.
	 * @return The key used to cache the image descriptor in the registry.
	 */
	abstract protected String getImageKey(FSTreeNode node);
	
	/**
	 * Return a mirror file that will be used to retrieve the image from.
	 * 
	 * @param node The file system tree node.
	 * @return The corresponding mirror file.
	 */
	abstract protected File getMirrorFile(FSTreeNode node);
	
	/**
	 * Get the node from the extension registry.
	 * 
	 * @param node The node for which the image is retrieved for.
	 * @return The image for this node.
	 */
	public Image getImage(FSTreeNode node) {
		String key = getImageKey(node);
		return UIPlugin.getImage(key);
	}

	/**
	 * Create an image file using "png" format 
	 * for the specified temp file.
	 * 
	 * @param icon The icon that is used for the tmp file.
	 * @param tmpfile The temp file.
	 */
	private void createImageFromIcon(Icon icon, String imgfile) {
		BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = bi.createGraphics();
		icon.paintIcon(dummyComponent, g, 0, 0);
		g.dispose();
		File imgFile = getImgFile(imgfile);
		try {
			ImageIO.write(bi, "png", imgFile); //$NON-NLS-1$
		}
		catch (IOException e) {
		}
	}

	/**
	 * Get the image file object for the specified temp file name.
	 * 
	 * @param tmpName The temp file name.
	 * @return The file object.
	 */
	private File getImgFile(String tmpName) {
		File imgFolder = getImgDir();
		return new File(imgFolder, tmpName + ".png"); //$NON-NLS-1$
	}

	/**
	 * Get the image's directory to hold the temporary images.
	 * 
	 * @return The directory
	 */
	private File getImgDir() {
		File file = CacheManager.getInstance().getCacheRoot();
		file = new File(file, ".img"); //$NON-NLS-1$
		if (!file.exists()) file.mkdirs();
		return file;
	}
	/**
	 * Send a notification to inform the file tree for changed images.
	 * 
	 * @param node The node whose image has changed.
	 * @param key The key used to store the images.
	 * @param oldImg The old image descriptor.
	 * @param newImg The new image descriptor.
	 */
	private void sendNotification(FSTreeNode node, String key, ImageDescriptor oldImg, ImageDescriptor newImg) {
		if (node.peerNode != null) {
			IViewerInput viewerInput = (IViewerInput) node.peerNode.getAdapter(IViewerInput.class);
			viewerInput.firePropertyChange(new PropertyChangeEvent(node, key, oldImg, newImg));
		}
	}
}
