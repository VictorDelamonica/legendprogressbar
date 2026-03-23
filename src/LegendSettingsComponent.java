import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class LegendSettingsComponent {
    private final JPanel panel;

    // --- Character selector ---
    private final ButtonGroup characterGroup;
    private final List<JRadioButton> characterRadioButtons = new ArrayList<>();

    // --- Items selector ---
    private ButtonGroup itemGroup;
    private final List<JRadioButton> itemRadioButtons = new ArrayList<>();

    public LegendSettingsComponent() {
        // ── Section 1: Character grid ───────────────────────────────────────
        JPanel characterGrid = new JPanel(new GridLayout(0, 2, JBUI.scale(8), JBUI.scale(8)));
        characterGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        characterGroup = new ButtonGroup();
        int charIconSize = JBUI.scale(36);

        for (LegendCharacter character : LegendCharacter.values()) {
            Icon scaled = scaleIcon(LegendIcons.getIcon(character, false), charIconSize);
            JRadioButton rb = new JRadioButton();
            rb.setActionCommand(character.name());
            rb.setToolTipText(character.getDisplayName());
            rb.setAlignmentY(Component.CENTER_ALIGNMENT);
            characterGroup.add(rb);
            characterGrid.add(buildTileRow(rb, scaled, character.getDisplayName()));
            characterRadioButtons.add(rb);
        }

        LegendCharacter selectedChar = LegendSettingsState.getInstance().getSelectedCharacter();
        setSelectedCharacter(selectedChar != null ? selectedChar : LegendCharacter.LINK);

        // ── Section 2: Items grid ───────────────────────────────────────────
        JPanel itemsGrid = buildItemsPanel();
        itemsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Assemble: single vertical stack ────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(8));

        content.add(buildSectionHeader("Character"));
        content.add(Box.createRigidArea(new Dimension(0, JBUI.scale(6))));
        content.add(characterGrid);

        content.add(Box.createRigidArea(new Dimension(0, JBUI.scale(14))));

        content.add(buildSectionHeader("Items"));
        content.add(Box.createRigidArea(new Dimension(0, JBUI.scale(6))));
        content.add(itemsGrid);

        panel = content;
    }

    // ── Section header: bold label + horizontal rule ───────────────────────

    private JPanel buildSectionHeader(String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD));

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);

        JPanel header = new JPanel(new GridBagLayout());
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, JBUI.scale(20)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = JBUI.insetsRight(JBUI.scale(6));
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        header.add(label, c);

        c.insets = JBUI.emptyInsets();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        header.add(sep, c);

        return header;
    }

    // ── Items panel ────────────────────────────────────────────────────────

    private JPanel buildItemsPanel() {
        JPanel container = new JPanel(new GridLayout(0, 2, JBUI.scale(8), JBUI.scale(8)));
        itemGroup = new ButtonGroup();
        int iconSize = JBUI.scale(24);

        List<String> paths = listIconsInFolder();

        if (paths.isEmpty()) {
            container.add(new JLabel("No item icons found in /infinit"));
            return container;
        }

        // Skip frame siblings with index >= 1 for all separator styles:
        //   underscore: nut_1.png, nut_2.png
        //   space:      Bombshu 1.png, Bombshu 2.png
        //   bare digit: nut1.png, nut2.png
        java.util.regex.Pattern frameSkip = java.util.regex.Pattern
                .compile("(?i)^.+([_ ]?[1-9]\\d*)\\.[^.]+$");

        for (String path : paths) {
            String filename = path.substring(path.lastIndexOf('/') + 1);

            // Skip any frame with index >= 1
            if (frameSkip.matcher(filename).matches()) continue;

            Icon scaled = scaleIcon(LegendIcons.getIconByPath(path), iconSize);

            JRadioButton rb = new JRadioButton();
            rb.setActionCommand(path);
            rb.setToolTipText(path);
            rb.setAlignmentY(Component.CENTER_ALIGNMENT);

            // Strip extension, then strip trailing _0 or 0 for a clean display name
            String nameNoExt = filename.contains(".")
                    ? filename.substring(0, filename.lastIndexOf('.'))
                    : filename;
            String displayName = nameNoExt
                    .replaceAll("_0$", "")    // nut_0  → nut
                    .replaceAll(" 0$", "")    // Bombshu 0 → Bombshu
                    .replaceAll("(?<![_\\s])0$", ""); // nut0 → nut (bare digit only)
            if (displayName.isEmpty()) displayName = nameNoExt;

            itemGroup.add(rb);
            container.add(buildTileRow(rb, scaled, displayName));
            itemRadioButtons.add(rb);
        }

        String savedPath = LegendSettingsState.getInstance().getSelectedItemPath();
        if (savedPath != null) {
            setSelectedItemPath(savedPath);
        } else if (!itemRadioButtons.isEmpty()) {
            itemRadioButtons.get(0).setSelected(true);
        }

        return container;
    }

    // ── Shared tile-row builder ────────────────────────────────────────────

    private JPanel buildTileRow(JRadioButton rb, Icon icon, String displayName) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        rb.setText("");
        rb.setAlignmentY(Component.CENTER_ALIGNMENT);
        row.add(rb);

        if (icon != null) {
            JLabel iconLabel = new JLabel(displayName, icon, SwingConstants.LEFT);
            iconLabel.setToolTipText(displayName);
            iconLabel.setIconTextGap(JBUI.scale(6));
            iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
            iconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { rb.setSelected(true); }
            });
            row.add(Box.createRigidArea(new Dimension(JBUI.scale(6), 0)));
            row.add(iconLabel);
        } else {
            JLabel label = new JLabel(displayName);
            label.setAlignmentY(Component.CENTER_ALIGNMENT);
            row.add(Box.createRigidArea(new Dimension(JBUI.scale(6), 0)));
            row.add(label);
        }

        row.setBorder(BorderFactory.createEmptyBorder(
                JBUI.scale(4), JBUI.scale(6), JBUI.scale(4), JBUI.scale(6)));
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { rb.setSelected(true); }
        });

        return row;
    }

    // ── Icon scaling ───────────────────────────────────────────────────────

    private static Icon scaleIcon(Icon icon, int targetSize) {
        if (icon == null) return null;
        if (icon.getIconWidth() == targetSize && icon.getIconHeight() == targetSize) return icon;
        int srcW = Math.max(1, icon.getIconWidth());
        int srcH = Math.max(1, icon.getIconHeight());
        BufferedImage src = new BufferedImage(srcW, srcH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = src.createGraphics();
        try { icon.paintIcon(new JLabel(), ig, 0, 0); } finally { ig.dispose(); }
        return new ImageIcon(src.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH));
    }

    // ── Resource folder scanner ────────────────────────────────────────────

    private List<String> listIconsInFolder() {
        List<String> results = new ArrayList<>();
        try {
            java.net.URL url = LegendIcons.class.getResource("/infinit");
            if (url == null) return results;
            String protocol = url.getProtocol();

            if ("file".equals(protocol)) {
                java.nio.file.Path dir = java.nio.file.Paths.get(url.toURI());
                try (java.util.stream.Stream<java.nio.file.Path> s = java.nio.file.Files.list(dir)) {
                    s.filter(p -> !java.nio.file.Files.isDirectory(p))
                     .map(p -> p.getFileName().toString())
                     .filter(n -> n.matches("(?i).+\\.(png|jpg|jpeg|gif|svg)"))
                     .filter(n -> !n.toLowerCase().startsWith("r"))
                     .forEach(n -> results.add("/infinit" + "/" + n));
                }
            } else if ("jar".equals(protocol)) {
                String path = url.getPath();
                int idx = path.indexOf("!");
                String jarPath = path.substring(5, idx);
                String prefix = "/infinit".replaceFirst("^/", "") + "/";
                try (java.util.jar.JarFile jar = new java.util.jar.JarFile(
                        java.net.URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                    java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        java.util.jar.JarEntry e = entries.nextElement();
                        String name = e.getName();
                        if (name.startsWith(prefix) && !name.endsWith("/")
                                && name.matches("(?i)" + prefix + ".+\\.(png|jpg|jpeg|gif|svg)$")) {
                            String fn = name.substring(prefix.length());
                            if (!fn.toLowerCase().startsWith("r")) {
                                results.add("/" + name);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) { /* silently return whatever was collected */ }
        return results;
    }

    // ── Public API ─────────────────────────────────────────────────────────

    public JPanel getPanel() { return panel; }

    // Character

    public LegendCharacter getSelectedCharacter() {
        ButtonModel sel = characterGroup.getSelection();
        if (sel == null) return LegendCharacter.LINK;
        try { return LegendCharacter.valueOf(sel.getActionCommand()); }
        catch (IllegalArgumentException e) { return LegendCharacter.LINK; }
    }

    public void setSelectedCharacter(LegendCharacter character) {
        if (character == null) return;
        for (JRadioButton rb : characterRadioButtons) {
            if (character.name().equals(rb.getActionCommand())) { rb.setSelected(true); return; }
        }
        if (!characterRadioButtons.isEmpty()) characterRadioButtons.get(0).setSelected(true);
    }

    // Item

    public String getSelectedItemPath() {
        ButtonModel sel = itemGroup.getSelection();
        return sel != null ? sel.getActionCommand() : null;
    }

    public void setSelectedItemPath(String path) {
        if (path == null) return;
        for (JRadioButton rb : itemRadioButtons) {
            if (path.equals(rb.getActionCommand())) { rb.setSelected(true); return; }
        }
        if (!itemRadioButtons.isEmpty()) itemRadioButtons.get(0).setSelected(true);
    }
}
