package accsswap;

import ga2.setting.GameSetting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class AccsSwapApp extends JFrame implements ActionListener {
    final JTextField pathField = new JTextField(guessGsPath(), 42);
    final SuggestField wearField;
    final SuggestField lookField;
    final JCheckBox keepName = new JCheckBox("\u4fdd\u7559\u539f\u88dd\u5907\u540d\u79f0", true);
    final JLabel status = new JLabel(" ");
    final JTextArea log = new JTextArea(10, 50);
    final JButton btnBrowse = new JButton("...");
    final JButton btnLoad = new JButton("\u52a0\u8f7d\u88dd\u5907\u8868");
    final JButton btnApply = new JButton("\u5e94\u7528\u5916\u89c2\u66ff\u6362");
    final JButton btnRestore = new JButton("\u8fd8\u539f\u5b98\u65b9\u5916\u89c2");

    AccsEntry[] catalog = new AccsEntry[0];

    public AccsSwapApp() {
        super("\u88dd\u5907\u5916\u89c2\u66ff\u6362");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 560);
        setLocationRelativeTo(null);

        wearField = new SuggestField();
        lookField = new SuggestField();

        JPanel pathRow = new JPanel(new BorderLayout(6, 0));
        pathRow.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        pathRow.add(new JLabel("gs.kxr:"), BorderLayout.WEST);
        pathRow.add(pathField, BorderLayout.CENTER);
        JPanel pathBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pathBtns.add(btnBrowse);
        pathBtns.add(btnLoad);
        pathRow.add(pathBtns, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(new JLabel("\u7a7f\u7740\u88dd\u5907\uff08\u88ab\u6539\u5916\u89c2\uff09:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(wearField, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        form.add(new JLabel("\u5916\u89c2\u6765\u6e90\u88dd\u5907:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(lookField, gc);
        gc.gridx = 1;
        gc.gridy = 2;
        form.add(keepName, gc);

        JPanel tip = new JPanel(new BorderLayout());
        tip.setBorder(BorderFactory.createEmptyBorder(0, 12, 4, 12));
        tip.add(new JLabel("\u53ef\u8f93\u5165\u540d\u79f0\u5173\u952e\u8bcd\uff08\u5982 \u52c7\u8005\uff09\u6216\u7f16\u53f7\uff08\u5982 81\uff09\u3002\u4e0b\u62c9\u8054\u60f3\u540e\u56de\u8f66/\u70b9\u51fb\u9009\u4e2d\u3002"), BorderLayout.NORTH);
        tip.add(new JLabel("\u6548\u679c\uff1a\u7a7f\u7740\u88dd\u5907\u663e\u793a\u5916\u89c2\u6765\u6e90\u7684\u6a21\u578b\uff0c\u6280\u80fd\u4ecd\u662f\u7a7f\u7740\u88dd\u5907\u7684\u3002"), BorderLayout.SOUTH);

        status.setFont(status.getFont().deriveFont(Font.BOLD, 14f));
        status.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        btnApply.setFont(btnApply.getFont().deriveFont(Font.BOLD, 14f));
        btnRestore.setFont(btnRestore.getFont().deriveFont(Font.BOLD, 14f));
        actions.add(btnApply);
        actions.add(btnRestore);

        log.setEditable(false);
        log.setLineWrap(true);

        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.NORTH);
        center.add(tip, BorderLayout.CENTER);
        center.add(status, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pathRow, BorderLayout.NORTH);
        getContentPane().add(center, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(actions, BorderLayout.NORTH);
        south.add(new JScrollPane(log), BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);

        btnBrowse.addActionListener(this);
        btnLoad.addActionListener(this);
        btnApply.addActionListener(this);
        btnRestore.addActionListener(this);

        refreshStatus();
        wearField.setEmptyCatalogHook(new Runnable() {
            public void run() {
                appendLog("\u8bf7\u5148\u70b9\u300c\u52a0\u8f7d\u88dd\u5907\u8868\u300d");
            }
        });
        lookField.setEmptyCatalogHook(new Runnable() {
            public void run() {
                appendLog("\u8bf7\u5148\u70b9\u300c\u52a0\u8f7d\u88dd\u5907\u8868\u300d");
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                autoLoadCatalog();
            }
        });
    }

    void autoLoadCatalog() {
        File gs = gsFile();
        if (gs.exists()) {
            loadCatalog();
        }
    }

    static String guessGsPath() {
        String env = System.getenv("GA2_BIN");
        if (env != null && env.length() > 0) {
            File f = new File(env, "gs.kxr");
            if (f.isFile()) {
                return f.getAbsolutePath();
            }
        }
        String drive = System.getenv("SystemDrive");
        if (drive == null || drive.length() == 0) {
            drive = "C:";
        }
        File common = new File(drive + "\\Cyberstep\\GetAmped2_TW\\bin\\gs.kxr");
        if (common.isFile()) {
            return common.getAbsolutePath();
        }
        return "";
    }

    File gsFile() {
        String p = pathField.getText().trim();
        if (p.toLowerCase().endsWith("gs.kx") && !p.toLowerCase().endsWith("gs.kxr")) {
            p = p + "r";
            pathField.setText(p);
        }
        return new File(p);
    }

    void appendLog(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    void refreshStatus() {
        File gs = gsFile();
        String st = SwapEngine.readState(gs);
        File bak = SwapEngine.backupFile(gs);
        if (st.length() == 0 || "OFF".equalsIgnoreCase(st)) {
            status.setText(bak.exists() ? "\u72b6\u6001\uff1a\u5df2\u8fd8\u539f / \u672a\u66ff\u6362\uff08\u6709\u5907\u4efd\uff09" : "\u72b6\u6001\uff1a\u672a\u66ff\u6362");
            status.setForeground(new Color(128, 0, 0));
        } else {
            status.setText("\u72b6\u6001\uff1a\u5df2\u66ff\u6362  " + st);
            status.setForeground(new Color(0, 128, 0));
        }
        btnRestore.setEnabled(bak.exists());
    }

    void loadCatalog() {
        try {
            File gs = gsFile();
            if (!gs.exists()) {
                JOptionPane.showMessageDialog(this, "\u627e\u4e0d\u5230 gs.kxr:\n" + gs.getAbsolutePath());
                return;
            }
            File bak = SwapEngine.backupFile(gs);
            File src = bak.exists() ? bak : gs;
            appendLog("\u8bfb\u53d6\u88dd\u5907\u8868: " + src.getAbsolutePath());
            GameSetting setting = GsIO.load(src.getAbsolutePath());
            catalog = GsIO.indexAccs(setting);
            wearField.setCatalog(catalog);
            lookField.setCatalog(catalog);
            appendLog("\u5df2\u52a0\u8f7d\u88dd\u5907 " + catalog.length + " \u4ef6\uff0cversion=" + setting.version);
            refreshStatus();
        } catch (Exception ex) {
            appendLog("\u52a0\u8f7d\u5931\u8d25: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "\u52a0\u8f7d\u5931\u8d25:\n" + ex.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnBrowse) {
            File startDir = gsFile().getParentFile();
            JFileChooser fc = new JFileChooser(startDir != null && startDir.isDirectory() ? startDir : null);
            fc.setFileFilter(new FileNameExtensionFilter("gs.kxr", "kxr"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        } else if (src == btnLoad) {
            loadCatalog();
        } else if (src == btnApply) {
            doApply();
        } else if (src == btnRestore) {
            doRestore();
        }
    }

    void doApply() {
        try {
            if (catalog.length == 0) {
                loadCatalog();
            }
            AccsEntry wear = wearField.getSelectedEntry();
            AccsEntry look = lookField.getSelectedEntry();
            if (wear == null) {
                JOptionPane.showMessageDialog(this, "\u8bf7\u586b\u5199\u5e76\u9009\u62e9\u300c\u7a7f\u7740\u88dd\u5907\u300d");
                return;
            }
            if (look == null) {
                JOptionPane.showMessageDialog(this, "\u8bf7\u586b\u5199\u5e76\u9009\u62e9\u300c\u5916\u89c2\u6765\u6e90\u88dd\u5907\u300d");
                return;
            }
            File gs = gsFile();
            if (!gs.exists()) {
                JOptionPane.showMessageDialog(this, "\u627e\u4e0d\u5230 gs.kxr");
                return;
            }
            String msg = "\u8bf7\u786e\u8ba4\u6e38\u620f\u5df2\u5b8c\u5168\u9000\u51fa\u3002\n\n\u7a7f\u7740: [" + wear.id + "] " + wear.name
                    + "\n\u5916\u89c2\u6765\u81ea: [" + look.id + "] " + look.name
                    + "\n\n\u6280\u80fd\u4ecd\u4e3a\u7a7f\u7740\u88dd\u5907\uff0c\u4ec5\u66ff\u6362\u6a21\u578b\u5916\u89c2\u3002";
            if (JOptionPane.showConfirmDialog(this, msg, "\u5e94\u7528\u5916\u89c2\u66ff\u6362", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }
            SwapEngine.applySwap(gs, wear.id, look.id, keepName.isSelected());
            appendLog("\u5df2\u66ff\u6362: " + wear.id + " <- " + look.id + " (" + look.name + ")");
            refreshStatus();
            JOptionPane.showMessageDialog(this, "\u66ff\u6362\u5b8c\u6210\u3002\u8bf7\u91cd\u65b0\u767b\u5f55\u6e38\u620f\u5e76\u88dd\u5907\u300c" + wear.name + "\u300d\u67e5\u770b\u3002");
        } catch (Exception ex) {
            appendLog("\u5931\u8d25: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "\u5931\u8d25:\n" + ex.getMessage());
        }
    }

    void doRestore() {
        try {
            File gs = gsFile();
            if (JOptionPane.showConfirmDialog(this, "\u8bf7\u786e\u8ba4\u6e38\u620f\u5df2\u9000\u51fa\u3002\n\u5c06\u7528\u5907\u4efd\u8fd8\u539f\u5168\u90e8\u88dd\u5907\u5916\u89c2\u3002", "\u8fd8\u539f",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }
            SwapEngine.restore(gs);
            appendLog("\u5df2\u8fd8\u539f\u5b98\u65b9\u5916\u89c2");
            refreshStatus();
            JOptionPane.showMessageDialog(this, "\u5df2\u8fd8\u539f\u3002\u8bf7\u91cd\u65b0\u767b\u5f55\u6e38\u620f\u3002");
        } catch (Exception ex) {
            appendLog("\u8fd8\u539f\u5931\u8d25: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "\u8fd8\u539f\u5931\u8d25:\n" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                }
                new AccsSwapApp().setVisible(true);
            }
        });
    }
}
