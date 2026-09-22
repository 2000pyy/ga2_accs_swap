package accsswap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.JPopupMenu;

public final class SuggestField extends JPanel {
    private final JTextField field = new JTextField();
    private final DefaultListModel model = new DefaultListModel();
    private final JList list = new JList(model);
    private final JPopupMenu popup = new JPopupMenu();
    private AccsEntry[] catalog = new AccsEntry[0];
    private boolean applying;
    private AccsEntry selected;
    private Runnable emptyCatalogHook;

    public SuggestField() {
        setLayout(new BorderLayout());
        add(field, BorderLayout.CENTER);
        list.setVisibleRowCount(10);
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(520, 200));
        popup.setFocusable(false);
        popup.add(sp);
        popup.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                onEdit();
            }
            public void removeUpdate(DocumentEvent e) {
                onEdit();
            }
            public void changedUpdate(DocumentEvent e) {
                onEdit();
            }
        });
        field.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!popup.isVisible()) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        onEdit();
                    }
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int i = list.getSelectedIndex();
                    if (i < model.getSize() - 1) {
                        list.setSelectedIndex(i + 1);
                        list.ensureIndexIsVisible(i + 1);
                    }
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int i = list.getSelectedIndex();
                    if (i > 0) {
                        list.setSelectedIndex(i - 1);
                        list.ensureIndexIsVisible(i - 1);
                    }
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    pickSelected();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                    e.consume();
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                pickSelected();
            }
        });
    }

    public void setEmptyCatalogHook(Runnable hook) {
        emptyCatalogHook = hook;
    }

    public JTextField getField() {
        return field;
    }

    public void setCatalog(AccsEntry[] catalog) {
        this.catalog = catalog == null ? new AccsEntry[0] : catalog;
    }

    public int catalogSize() {
        return catalog.length;
    }

    public void setText(String text) {
        applying = true;
        field.setText(text == null ? "" : text);
        applying = false;
    }

    public String getText() {
        return field.getText();
    }

    public AccsEntry getSelectedEntry() {
        AccsEntry byText = SwapEngine.resolve(catalog, field.getText());
        if (byText != null) {
            return byText;
        }
        return selected;
    }

    public void addActionListener(ActionListener l) {
        field.addActionListener(l);
    }

    private void onEdit() {
        if (applying) {
            return;
        }
        selected = null;
        String q = field.getText();
        model.clear();
        if (q == null || q.trim().length() < 1) {
            popup.setVisible(false);
            return;
        }
        if (catalog.length == 0) {
            popup.setVisible(false);
            if (emptyCatalogHook != null) {
                SwingUtilities.invokeLater(emptyCatalogHook);
            }
            return;
        }
        List hits = SwapEngine.search(catalog, q, 50);
        if (hits.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        for (int i = 0; i < hits.size(); i++) {
            model.addElement(hits.get(i));
        }
        list.setSelectedIndex(0);
        showPopup();
    }

    private void showPopup() {
        if (!field.isShowing()) {
            return;
        }
        popup.setPopupSize(Math.max(520, field.getWidth()), 200);
        popup.show(field, 0, field.getHeight());
        field.requestFocusInWindow();
    }

    private void pickSelected() {
        Object v = list.getSelectedValue();
        if (!(v instanceof AccsEntry)) {
            return;
        }
        AccsEntry e = (AccsEntry) v;
        selected = e;
        applying = true;
        field.setText(e.display());
        applying = false;
        popup.setVisible(false);
        ActionListener[] ls = field.getActionListeners();
        for (int i = 0; i < ls.length; i++) {
            ls[i].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "pick"));
        }
    }
}