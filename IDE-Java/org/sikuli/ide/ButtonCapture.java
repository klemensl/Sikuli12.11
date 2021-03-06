/*
 * Copyright 2010-2011, Sikuli.org
 * Released under the MIT License.
 *
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.text.*;
import org.sikuli.script.OverlayCapturePrompt;
import org.sikuli.script.EventObserver;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.EventSubject;
import org.sikuli.ide.util.Utils;
import org.sikuli.script.Debug;

class ButtonCapture extends ButtonOnToolbar implements ActionListener, Cloneable, EventObserver {

	protected Element _line;
	protected EditorPane _codePane;
	protected boolean _isCapturing;

	public ButtonCapture() {
		super();
		URL imageURL = SikuliIDE.class.getResource("/icons/camera-icon.png");
		setIcon(new ImageIcon(imageURL));
		PreferencesUser pref = PreferencesUser.getInstance();
		String strHotkey = Utils.convertKeyToText(
						pref.getCaptureHotkey(), pref.getCaptureHotkeyModifiers());
		setToolTipText(SikuliIDE._I("btnCaptureHint", strHotkey));
		setText(SikuliIDE._I("btnCaptureLabel"));
		//setBorderPainted(false);
		//setMaximumSize(new Dimension(26,26));
		addActionListener(this);
		_line = null;
	}

	public ButtonCapture(EditorPane codePane, Element elmLine) {
		this();
		_line = elmLine;
		_codePane = codePane;
		setUI(UIManager.getUI(this));
		setBorderPainted(true);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setText(null);
		URL imageURL = SikuliIDE.class.getResource("/icons/capture.png");
		setIcon(new ImageIcon(imageURL));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Debug.log(2, "capture!");
		captureWithAutoDelay();
	}

	public void captureWithAutoDelay() {
		if (_isCapturing) {
			return;
		}
		PreferencesUser pref = PreferencesUser.getInstance();
		int delay = (int) (pref.getCaptureDelay() * 1000.0) + 1;
		capture(delay);
	}

	public void capture(final int delay) {
		if (_isCapturing) {
			return;
		}
		_isCapturing = true;
		Thread t = new Thread("capture") {
			@Override
			public void run() {
				SikuliIDE ide = SikuliIDE.getInstance();
				if (delay != 0) {
					ide.setVisible(false);
				}
				try {
					Thread.sleep(delay);
				} catch (Exception e) {
				}
				OverlayCapturePrompt p = new OverlayCapturePrompt(null, ButtonCapture.this);
				p.prompt("Select an image");
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
				if (delay != 0) {
					ide.setVisible(true);
				}
				ide.requestFocus();
			}
		};
		t.start();
	}

	//<editor-fold defaultstate="collapsed" desc="RaiMan not used">
	/*public boolean hasNext() {
	 * return false;
	 * }*/
	/*public CaptureButton getNextDiffButton() {
	 * return null;
	 * }*/
	/*public void setParentPane(SikuliPane parent) {
	 * _codePane = parent;
	 * }*/
	/*public void setDiffMode(boolean flag) {
	 * }*/
	/*public void setSrcElement(Element elmLine) {
	 * _line = elmLine;
	 * }*/
	//</editor-fold>

	@Override
	public void update(EventSubject s) {
		if (s instanceof OverlayCapturePrompt) {
			OverlayCapturePrompt cp = (OverlayCapturePrompt) s;
			ScreenImage simg = cp.getSelection();
			String filename = null;
			EditorPane pane = SikuliIDE.getInstance().getCurrentCodePane();

			if (simg != null) {
				int naming = PreferencesUser.getInstance().getAutoNamingMethod();
				if (naming == PreferencesUser.AUTO_NAMING_TIMESTAMP) {
					filename = Utils.getTimestamp();
				} else if (naming == PreferencesUser.AUTO_NAMING_OCR) {
//RaiMan not used            filename = NamingPane.getFilenameFromImage(simg.getImage());
					if (filename == null || filename.length() == 0) {
						filename = Utils.getTimestamp();
					}
				} else {
//RaiMan not used            String hint = NamingPane.getFilenameFromImage(simg.getImage());
					filename = getFilenameFromUser("");//RaiMan changed getFilenameFromUser(hint);
				}

				if (filename != null) {
					String fullpath =
									Utils.saveImage(simg.getImage(), filename, pane.getSrcBundle());
					if (fullpath != null) {
						//String fullpath = pane.getFileInBundle(filename).getAbsolutePath();
						captureCompleted(Utils.slashify(fullpath, false), cp);
						return;
					}
				}
			}

			captureCompleted(null, cp);
		}
	}

	private String getFilenameFromUser(String hint) {
		return (String) JOptionPane.showInputDialog(
						_codePane,
						SikuliIDEI18N._I("msgEnterScreenshotFilename"),
						SikuliIDEI18N._I("dlgEnterScreenshotFilename"),
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						hint);
	}

	public void captureCompleted(String imgFullPath, OverlayCapturePrompt prompt) {
		prompt.close();

		if (imgFullPath != null) {
			Debug.log(2, "captureCompleted: " + imgFullPath);
			Element src = getSrcElement();
			if (src == null) {
				if (_codePane == null) {
					insertAtCursor(SikuliIDE.getInstance().getCurrentCodePane(), imgFullPath);
				} else {
					insertAtCursor(_codePane, imgFullPath);
				}
			} else {
				int start = src.getStartOffset();
				int end = src.getEndOffset();
				int old_sel_start = _codePane.getSelectionStart(),
								old_sel_end = _codePane.getSelectionEnd();
				try {
					StyledDocument doc = (StyledDocument) src.getDocument();
					String text = doc.getText(start, end - start);
					Debug.log(3, text);
					for (int i = start; i < end; i++) {
						Element elm = doc.getCharacterElement(i);
						if (elm.getName().equals(StyleConstants.ComponentElementName)) {
							AttributeSet attr = elm.getAttributes();
							Component com = StyleConstants.getComponent(attr);
							if (com instanceof ButtonCapture) {
								Debug.log(5, "button is at " + i);
								int oldCaretPos = _codePane.getCaretPosition();
								_codePane.select(i, i + 1);
								PatternImageButton icon = new PatternImageButton(_codePane, imgFullPath);
								_codePane.insertComponent(icon);
								_codePane.setCaretPosition(oldCaretPos);
								break;
							}
						}
					}
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
				_codePane.select(old_sel_start, old_sel_end);
				_codePane.requestFocus();
			}
		}
		_isCapturing = false;
	}

	private Element getSrcElement() {
		return _line;
	}

	protected void insertAtCursor(EditorPane pane, String imgFilename) {
		PatternImageButton icon = new PatternImageButton(pane, imgFilename);
		pane.insertComponent(icon);
		pane.requestFocus();
	}

	@Override
	public String toString() {
		return "\"__SIKULI-CAPTURE-BUTTON__\"";
	}
}
