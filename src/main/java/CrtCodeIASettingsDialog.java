import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.DialogWrapper;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;

import javax.swing.*;
import java.awt.*;


public class CrtCodeIASettingsDialog extends DialogWrapper {

    private JTextField textField;
    private JPanel settingsPanel;

    private JTextField urlField;
    private JTextField tokenField;

    private JTextField modelField;
    private JTextField roleField;

    CrtCodeIASettingsState state;


    public CrtCodeIASettingsDialog() {
        super(true); // use current window as parent
        setTitle("Test DialogWrapper");
        state = CrtCodeIASettingsState.getInstance();
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        String url = state.getUrl();
        String token = state.getToken();
        String model = state.getModel();
        String role = state.getRole();

        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 2, 2));

        urlField = new JTextField(url,30);
        tokenField = new JTextField(token,30);
        modelField = new JTextField(model,30);
        roleField = new JTextField(role,30);

        JPanel urlLine = newLine(
                settingsPanel,
                "URL:",
                urlField
        );
        JPanel tokenLine = newLine(
                urlLine,
                "Token:",
                tokenField
        );
        JPanel modelLine = newLine(
                tokenLine,
                "Model:",
                modelField
        );
        JPanel roleLine = newLine(
                modelLine,
                "Role:",
                roleField
        );

        return roleLine;
    }


    public JPanel newLine(JPanel settingsPanel,String label, JTextField textField){

        JLabel tokenLabel = new JLabel(label);
        settingsPanel.add(tokenLabel);
        settingsPanel.add(textField);
        return settingsPanel;
    }

    public boolean isModified() {
        return !urlField.getText().equals(state.getUrl()) ||
                !tokenField.getText().equals(state.getToken()) ||
                !modelField.getText().equals(state.getModel()) ||
                !roleField.getText().equals(state.getRole());
    }



    public void apply() throws ConfigurationException {
        String token = tokenField.getText();
        String url = urlField.getText();
        String role = roleField.getText();
        String model = modelField.getText();

        state.setRole(role);
        state.setModel(model);
        state.setToken(token);
        state.setUrl(url);
    }
    @Override
    public void doOKAction() {
            try {
                if (isModified()) {
                    apply();
                }
                super.doOKAction();

            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }

    }


    public String getText() {
        return textField.getText();
    }

}
