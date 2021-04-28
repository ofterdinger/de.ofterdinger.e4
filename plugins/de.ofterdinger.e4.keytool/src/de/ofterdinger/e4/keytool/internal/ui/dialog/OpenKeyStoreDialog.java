package de.ofterdinger.e4.keytool.internal.ui.dialog;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.eclipse.swt.SWT.OPEN;
import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import de.ofterdinger.e4.keytool.internal.ui.util.KeystoreUIHelper;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class OpenKeyStoreDialog extends Dialog {

  private String filename;
  private Text filenameText;
  private Image[] images;
  private String keystoreType;
  private Combo keystoreTypeCombo;
  private String password;
  private Text passwordText;

  public OpenKeyStoreDialog(Shell parentShell) {
    super(parentShell);
  }

  @Override
  public boolean close() {
    if (this.images != null) {
      int i = 0;
      while (i < this.images.length) {
        this.images[i].dispose();
        ++i;
      }
    }
    return super.close();
  }

  public String getFilename() {
    return this.filename;
  }

  public KeystoreType getKeystoreType() {
    return KeystoreType.getInstance(this.keystoreType);
  }

  public String getPassword() {
    return this.password;
  }

  private void handleBrowse(SelectionEvent e) {
    String file;
    if (e.widget instanceof Button) {
      file = new FileDialog(getShell(), OPEN).open();

      if (isNotBlank(file)) {
        this.filenameText.setText(file);
      }
    }
  }

  @Override
  protected final void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Open keystorefile");
    IProduct product = Platform.getProduct();
    if (product != null) {
      String[] imageURLs = parseCSL(product.getProperty("windowImages"));
      if (isNotEmpty(imageURLs)) {
        this.images = new Image[imageURLs.length];
        int i = 0;
        while (i < imageURLs.length) {
          String url = imageURLs[i];
          ImageDescriptor descriptor =
              imageDescriptorFromPlugin(product.getDefiningBundle().getSymbolicName(), url);
          this.images[i] = descriptor.createImage(true);
          ++i;
        }
        newShell.setImages(this.images);
      }
    }
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, 0, "&Load", true);
    createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = new Composite(parent, 0);
    GridLayout layout = new GridLayout(3, false);
    composite.setLayout(layout);
    GridData dataSpan2 = new GridData(4, 4, false, false, 2, 1);
    GridData dataNoSpan = new GridData(1, 2, false, false);
    dataNoSpan.minimumWidth = 40;

    Label keystoreTypeLabel = new Label(composite, 0);
    keystoreTypeLabel.setText("&Type:");
    keystoreTypeLabel.setLayoutData(dataNoSpan);

    this.keystoreTypeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
    GridData gridData = new GridData(4, 4, true, false, 2, 1);
    gridData.widthHint = this.convertHeightInCharsToPixels(20);
    this.keystoreTypeCombo.setLayoutData(gridData);

    Label filenameLabel = new Label(composite, 0);
    filenameLabel.setText("&Filename:");
    filenameLabel.setLayoutData(new GridData(1, 2, false, false));

    this.filenameText = new Text(composite, 2048);
    this.filenameText.setLayoutData(new GridData(4, 2, true, false));

    Button button = new Button(composite, SWT.PUSH);
    button.setText("Browse...");
    button.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleBrowse));

    Label passwordLabel = new Label(composite, 0);
    passwordLabel.setText("&Password:");
    passwordLabel.setLayoutData(dataNoSpan);

    this.passwordText = new Text(composite, SWT.PASSWORD);
    this.passwordText.setLayoutData(dataSpan2);

    initialize();
    composite.redraw();
    return composite;
  }

  protected void initialize() {
    KeystoreUIHelper.populateAvailableKeystoreTypes(this.keystoreTypeCombo);
    this.passwordText.setText(EMPTY);
  }

  @Override
  protected void okPressed() {
    this.filename = this.filenameText.getText();
    this.password = this.passwordText.getText();
    this.keystoreType = this.keystoreTypeCombo.getText();
    super.okPressed();
  }

  private static String[] parseCSL(String csl) {
    if (csl == null) {
      return EMPTY_STRING_ARRAY;
    }
    StringTokenizer tokens = new StringTokenizer(csl, ",");
    ArrayList<String> array = new ArrayList<>(10);
    while (tokens.hasMoreTokens()) {
      array.add(tokens.nextToken().trim());
    }
    return array.toArray(new String[array.size()]);
  }
}
