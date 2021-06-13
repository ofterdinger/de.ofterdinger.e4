package de.ofterdinger.ide.eclipse.keytool.internal.ui.wizard;

import static de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.eclipse.swt.SWT.FILL;

import de.ofterdinger.ide.eclipse.keytool.internal.KeystoreFile;
import de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.KeyStoreView;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.util.TreeParent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractWizardPage extends WizardPage {
  static final String CREATE_A_NEW_KEYSTORE_TEXT = "<Create a new keystore>";
  private int colspan = 0;
  private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
  private boolean editable = true;
  private ModifyListener modifyListener = null;

  protected AbstractWizardPage(String pageName) {
    super(pageName);
  }

  public DateFormat getDateFormat() {
    return this.dateFormat;
  }

  public boolean isEditable() {
    return this.editable;
  }

  public void setColspan(int colspan) {
    this.colspan = colspan;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  protected void addBrowseButton(Composite composite, Text filenameText) {
    Button button = new Button(composite, SWT.PUSH);
    button.setText("Browse...");
    button.addSelectionListener(
        SelectionListener.widgetSelectedAdapter(
            e -> AbstractWizardPage.this.handleBrowse(e, filenameText)));
  }

  protected Combo addOpenKeystores(Composite container, TreeParent selection) {
    Label label = new Label(container, 0);
    label.setText("Keystore");
    GridData gridData = new GridData(4, 4, true, false, 1, 1);
    Combo keystoreCombo = new Combo(container, 12);
    keystoreCombo.setLayoutData(gridData);
    keystoreCombo.addModifyListener(getModifyListener());
    boolean hasSelection = false;
    String selectedFilename;
    keystoreCombo.add(CREATE_A_NEW_KEYSTORE_TEXT);
    if (selection != null) {
      hasSelection = true;
      selectedFilename = selection.getKeystoreFilename();
    } else {
      selectedFilename = CREATE_A_NEW_KEYSTORE_TEXT;
      keystoreCombo.select(0);
    }
    List<KeystoreFile> keystores = KeyStoreView.getKeystores();
    if (keystores != null) {
      int i = 0;
      while (i < keystores.size()) {
        String keystorefilename = keystores.get(i).getKeystorefilename();
        keystoreCombo.add(keystorefilename);
        if (hasSelection && selectedFilename.equals(keystorefilename)) {
          keystoreCombo.select(i + 1);
        }
        ++i;
      }
    }
    return keystoreCombo;
  }

  protected void checkDate(Text dateText, String fieldName) {
    try {
      if (dateText == null || dateText.getText().length() != 10) {
        updateStatus("'" + fieldName + "' is not a valid date");
      } else {
        this.dateFormat.parse(dateText.getText());
      }
    } catch (ParseException parseException) {
      KeytoolPlugin.getDefault().getLog().warn(parseException.getMessage(), parseException);
      updateStatus("'" + fieldName + "' is not a valid date");
    }
  }

  protected void checkStatus(Combo combo, String message) {
    this.checkStatus(combo == null ? null : combo.getText(), message);
  }

  protected void checkStatus(Text text, String message) {
    if (text != null) {
      this.checkStatus(text.getText(), message);
    } else {
      updateStatus(message);
    }
  }

  protected abstract void dialogChanged();

  protected String[] getExtensions() {
    return new String[] {"*.cer", "*.pfx"};
  }

  protected ModifyListener getModifyListener() {
    if (this.modifyListener == null) {
      this.modifyListener = e -> dialogChanged();
    }
    return this.modifyListener;
  }

  private void handleBrowse(SelectionEvent e, Text filenameText) {
    if (e.widget instanceof Button) {
      FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
      dialog.setFilterExtensions(getExtensions());
      String file = dialog.open();
      if (StringUtils.isNotBlank(file)) {
        setFilenameText(filenameText, file);
      }
      dialogChanged();
    }
  }

  protected Text makeLine(Composite composite, String labelText, String value) {
    return this.makeLine(composite, labelText, value, SWT.BORDER);
  }

  protected Text makeLine(Composite composite, String labelText, String value, int style) {
    Label label = new Label(composite, 0);
    label.setText(labelText);
    GridData gridData = new GridData(FILL, FILL, true, false);
    if (this.colspan > 0) {
      gridData.horizontalSpan = this.colspan;
    }
    Text text = new Text(composite, style);
    text.setLayoutData(gridData);
    text.setText(value == null ? EMPTY : value);
    text.setEditable(this.editable);
    text.addModifyListener(getModifyListener());
    return text;
  }

  protected Date parseDate(Text text) {
    try {
      return getDateFormat().parse(text.getText());
    } catch (ParseException e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
      return null;
    }
  }

  protected void resetStatus() {
    setErrorMessage(null);
    setPageComplete(false);
  }

  protected void setFilenameText(Text filenameText, String text) {
    filenameText.setText(text);
  }

  protected void updateStatus(String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  private void checkStatus(String txt, String message) {
    if (txt == null || txt.length() == 0) {
      updateStatus(message);
    }
  }
}
