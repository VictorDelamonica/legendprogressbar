import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LegendOfZeldaProgressBarUi extends BasicProgressBarUI {

    // frames per paint-tick before advancing to the next animation frame
    private static final int ANIM_SPEED = 6;

    // cache: base path (e.g. "/infinit/nut_0.png") → ordered array of forward frames
    private static final Map<String, Icon[]> frameCache = new ConcurrentHashMap<>();

    private volatile int offset = 0;
    private volatile int offset2 = 0;
    private volatile int velocity = 1;
    private volatile int animTick = 0;   // counts paint calls; drives frame index

    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
    public static ComponentUI createUI(JComponent c) {
        c.setBorder(JBUI.Borders.empty().asUIResource());
        return new LegendOfZeldaProgressBarUi();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(super.getPreferredSize(c).width, JBUI.scale(24));
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        progressBar.addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { super.componentShown(e); }
            @Override public void componentHidden(ComponentEvent e) { super.componentHidden(e); }
        });
    }

    // ── Animation frame resolution ─────────────────────────────────────────

    /**
     * Returns all animation frames for the given selected path.
     * <p>
     * Naming convention — frames must follow one of these patterns:
     *   /infinit/nut_0.png, /infinit/nut_1.png, ...   (underscore + index)
     *   /infinit/nut0.png,  /infinit/nut1.png,  ...   (digit suffix)
     * <p>
     * If no siblings are found, returns a single-element array with the icon itself.
     * Mirrored variants (prefixed with "r") are excluded — they are resolved at paint time.
     */
    private static Icon[] resolveFrames(String path) {
        return frameCache.computeIfAbsent(path, p -> {
            // Try to detect base name and index from the filename
            // e.g. "/infinit/nut_0.png" → folder="/infinit/", base="nut_", ext=".png", startIndex=0
            String folder;
            String filename;
            int lastSlash = p.lastIndexOf('/');
            if (lastSlash >= 0) {
                folder = p.substring(0, lastSlash + 1);   // "/infinit/"
                filename = p.substring(lastSlash + 1);     // "nut_0.png"
            } else {
                folder = "/";
                filename = p;
            }

            int dotIdx = filename.lastIndexOf('.');
            String nameNoExt = dotIdx >= 0 ? filename.substring(0, dotIdx) : filename;
            String ext       = dotIdx >= 0 ? filename.substring(dotIdx)    : "";

            // Match trailing separator+N where separator is _, space, or nothing
            // e.g. "nut_0", "Bombshu 0", "nut0"
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("^(.*?)([_ ]?)(\\d+)$")
                    .matcher(nameNoExt);

            if (!m.matches()) {
                // No numeric suffix — single frame, check resource exists first
                if (LegendIcons.class.getResource(p) == null) return new Icon[0];
                Icon icon = LegendIcons.getIconByPath(p);
                return new Icon[]{icon};
            }

            String base      = m.group(1);   // "nut" or "Bombshu"
            String sep       = m.group(2);   // "_", " ", or ""
            // startIdx unused but kept for clarity
            // int startIdx = Integer.parseInt(m.group(3));

            // Scan forward from index 0 using resource existence — IconLoader never returns null
            List<Icon> frames = new ArrayList<>();
            int MAX_FRAMES = 64; // hard safety cap
            for (int i = 0; i < MAX_FRAMES; i++) {
                String frameName = base + sep + i + ext;
                String framePath = folder + frameName;
                // Only load if the resource actually exists
                if (LegendIcons.class.getResource(framePath) == null) break;
                Icon icon = LegendIcons.getIconByPath(framePath);
                frames.add(icon);
            }

            if (frames.isEmpty()) {
                if (LegendIcons.class.getResource(p) == null) return new Icon[0];
                Icon icon = LegendIcons.getIconByPath(p);
                return new Icon[]{icon};
            }
            return frames.toArray(new Icon[0]);
        });
    }

    /** Resolves mirrored frames, falling back to forward frames if none exist. */
    private static Icon[] resolveMirroredFrames(String basePath) {
        int lastSlash = basePath.lastIndexOf('/');
        String folder   = lastSlash >= 0 ? basePath.substring(0, lastSlash + 1) : "/";
        String filename = lastSlash >= 0 ? basePath.substring(lastSlash + 1) : basePath;
        if (filename.toLowerCase().startsWith("r")) return resolveFrames(basePath);
        String mirroredPath = folder + "r" + filename;
        Icon[] mirrored = resolveFrames(mirroredPath);
        if (mirrored.length == 0) return resolveFrames(basePath); // fallback
        return mirrored;
    }

    // ── Paint indeterminate ────────────────────────────────────────────────

    @Override
    protected void paintIndeterminate(Graphics g2d, JComponent c) {
        if (!(g2d instanceof Graphics2D g)) return;

        Insets b = progressBar.getInsets();
        int barRectWidth  = progressBar.getWidth()  - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top   + b.bottom);
        if (barRectWidth <= 0 || barRectHeight <= 0) return;

        LegendCharacter character    = getSelectedCharacter();
        String          selectedPath = LegendSettingsState.getInstance().getSelectedItemPath();

        int w = c.getWidth();
        int h = c.getPreferredSize().height;
        if (isOdd(c.getHeight() - h)) h++;

        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
        g.translate(0, (c.getHeight() - h) / 2);

        final float R = (float)JBUI.scale(8);

        // Advance movement
        offset = (offset + 1) % getPeriodLength();
        offset2 += velocity;
        if (offset2 <= 2) {
            offset2 = 2;
            velocity = 1;
        } else if (offset2 >= w - JBUI.scale(15)) {
            offset2 = w - JBUI.scale(15);
            velocity = -1;
        }

        // Advance animation tick
        animTick++;

        g.draw(new RoundRectangle2D.Float(1f, 1f, w - 2f - 1f, h - 2f - 1f, R, R));
        g.translate(0, -(c.getHeight() - h) / 2);

        int trackX = JBUI.scale(2);
        int trackY = Math.max(0, (c.getHeight() - h) / 2 + JBUI.scale(2));
        int trackW = Math.max(1, w - JBUI.scale(5));
        int trackH = Math.max(1, h - JBUI.scale(5));

        if (selectedPath != null) {
            // Resolve the correct frame array (mirrored or forward)
            Icon[] frames = velocity < 0
                    ? resolveMirroredFrames(selectedPath)
                    : resolveFrames(selectedPath);

            if (frames.length > 0) {
                int frameIndex = (animTick / ANIM_SPEED) % frames.length;
                Icon frame = frames[frameIndex];
                paintIconAtCenter(g, frame, trackX, trackY, trackW, trackH, offset2);
            } else {
                // No frames found — fall back to character
                paintCharacterIcon(g, character, trackX, trackY, trackW, trackH, offset2, velocity < 0);
            }
        } else {
            paintCharacterIcon(g, character, trackX, trackY, trackW, trackH, offset2, velocity < 0);
        }

        if (progressBar.isStringPainted()) {
            paintProgressString(g, b.left, b.top, barRectWidth, barRectHeight);
        }
        config.restore();
    }

    /** Inline string painter — avoids the SDK-overridden paintString signature. */
    private void paintProgressString(Graphics2D g, int x, int y, int w, int h) {
        String text = progressBar.getString();
        if (text == null || text.isEmpty()) return;
        g.setFont(progressBar.getFont());
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(progressBar.getForeground());
        g.drawString(text, tx, ty);
    }

    // ── Paint determinate ──────────────────────────────────────────────────

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        if (!(g instanceof Graphics2D g2)) return;

        if (progressBar.getOrientation() != SwingConstants.HORIZONTAL || !c.getComponentOrientation().isLeftToRight()) {
            super.paintDeterminate(g, c);
            return;
        }
        Insets b = progressBar.getInsets();
        int w = progressBar.getWidth();
        int h = progressBar.getPreferredSize().height;
        if (isOdd(c.getHeight() - h)) h++;

        int barRectWidth  = w - (b.right + b.left);
        int barRectHeight = h - (b.top   + b.bottom);
        if (barRectWidth <= 0 || barRectHeight <= 0) return;

        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
        LegendCharacter character = getSelectedCharacter();

        Container parent = c.getParent();
        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();

        g.setColor(background);
        if (c.isOpaque()) g.fillRect(0, 0, w, h);

        final float R   = (float)JBUI.scale(8);
        final float R2  = (float)JBUI.scale(9);
        final float off = (float)JBUI.scale(1);

        Color progressColor = brighten(character.getBackgroundStart());
        g2.setColor(progressColor);

        float filledWidth = Math.max(0, amountFull - (float)JBUI.scale(5));
        int trackX = Math.round(2f * off);
        int trackY = Math.round(2f * off);
        int trackW = Math.max(1, w - trackX * 2);
        int trackH = Math.max(1, h - trackY * 2);

        if (filledWidth > 0) {
            Shape oldClip = g2.getClip();
            g2.clip(new Rectangle2D.Float(trackX, trackY, filledWidth, trackH));
            g2.fill(new RoundRectangle2D.Float(trackX, trackY, trackW, trackH, R, R));
            g2.setClip(oldClip);
        }

        g2.setColor(g2.getColor().darker());
        g2.draw(new RoundRectangle2D.Float(off, off, w - 2f * off - 1f, h - 2f * off - 1f, R2, R2));

        paintCharacterIcon(g2, character, trackX, trackY, trackW, trackH, amountFull, false);

        if (progressBar.isStringPainted()) {
            g2.setFont(progressBar.getFont());
            String progressString = progressBar.getString();
            g2.setColor(progressBar.getForeground());
            Point renderLocation = getStringPlacement(g2, progressString,
                    b.left, b.top, barRectWidth, barRectHeight);
            Shape oldClip = g2.getClip();
            g2.setClip(new Rectangle2D.Float(off, off, amountFull - off, h));
            g2.setColor(JBColor.WHITE);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
            g2.setClip(new Rectangle2D.Float(amountFull, 0, w - amountFull, h));
            g2.setColor(progressBar.getForeground());
            BasicGraphicsUtils.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
            g2.setClip(oldClip);
        }
    }

    @Override
    protected int getBoxLength(int availableLength, int otherDimension) {
        return availableLength;
    }

    private int getPeriodLength() {
        return JBUI.scale(16);
    }

    // ── Icon painters ──────────────────────────────────────────────────────

    private void paintCharacterIcon(Graphics2D g2,
                                    LegendCharacter character,
                                    int trackX, int trackY,
                                    int trackWidth, int trackHeight,
                                    int centerX, boolean mirrored) {
        Icon icon = LegendIcons.getIcon(character, mirrored);
        if (icon == null) icon = LegendIcons.getIcon(LegendCharacter.LINK, mirrored);

        int sourceIconWidth  = icon != null ? icon.getIconWidth()  : JBUI.scale(12);
        int sourceIconHeight = icon != null ? icon.getIconHeight() : JBUI.scale(12);

        int maxIconHeight = Math.max(1, Math.round(trackHeight * 1.15f));
        int maxIconWidth  = Math.max(1, Math.min(trackWidth, JBUI.scale(48)));
        float ratio = Math.min((float) maxIconWidth / sourceIconWidth, (float) maxIconHeight / sourceIconHeight);
        ratio = Math.max(0.01f, Math.min(1f, ratio));

        int iconWidth  = Math.max(1, Math.round(sourceIconWidth  * ratio));
        int iconHeight = Math.max(1, Math.round(sourceIconHeight * ratio));
        int minCenter  = trackX + iconWidth / 2;
        int maxCenter  = trackX + trackWidth - iconWidth / 2;
        int clampedCenterX = Math.max(minCenter, Math.min(centerX, maxCenter));
        int iconX = clampedCenterX - iconWidth / 2;
        int iconY = trackY + ((trackHeight - iconHeight) / 2) - JBUI.scale(2);

        Shape oldClip = g2.getClip();
        g2.clip(new Rectangle2D.Float(
                trackX - JBUI.scale(4), trackY - JBUI.scale(8),
                trackWidth + JBUI.scale(8), trackHeight + JBUI.scale(16)));
        if (icon != null) {
            paintIconScaled(g2, icon, iconX, iconY, iconWidth, iconHeight, sourceIconWidth, sourceIconHeight);
        } else {
            paintFallbackGlyph(g2, iconX, iconY, iconWidth, iconHeight);
        }
        g2.setClip(oldClip);
    }

    private void paintIconAtCenter(Graphics2D g2,
                                   Icon icon,
                                   int trackX, int trackY,
                                   int trackWidth, int trackHeight,
                                   int centerX) {
        if (icon == null) return;

        int sourceIconWidth  = icon.getIconWidth();
        int sourceIconHeight = icon.getIconHeight();

        int maxIconHeight = Math.max(1, Math.round(trackHeight * 1.15f));
        int maxIconWidth  = Math.max(1, Math.min(trackWidth, JBUI.scale(48)));
        float ratio = Math.min((float) maxIconWidth / sourceIconWidth, (float) maxIconHeight / sourceIconHeight);
        ratio = Math.max(0.01f, ratio); // allow upscaling — no upper cap

        int iconWidth  = Math.max(1, Math.round(sourceIconWidth  * ratio));
        int iconHeight = Math.max(1, Math.round(sourceIconHeight * ratio));
        int minCenter  = trackX + iconWidth / 2;
        int maxCenter  = trackX + trackWidth - iconWidth / 2;
        int clampedCenterX = Math.max(minCenter, Math.min(centerX, maxCenter));
        int iconX = clampedCenterX - iconWidth / 2;
        int iconY = trackY + (trackHeight - iconHeight) / 2; // true vertical center

        Shape oldClip = g2.getClip();
        g2.clip(new Rectangle2D.Float(
                trackX - JBUI.scale(4), trackY - JBUI.scale(8),
                trackWidth + JBUI.scale(8), trackHeight + JBUI.scale(16)));
        paintIconScaled(g2, icon, iconX, iconY, iconWidth, iconHeight, sourceIconWidth, sourceIconHeight);
        g2.setClip(oldClip);
    }

    private void paintIconScaled(Graphics2D g2,
                                 Icon icon,
                                 int x, int y,
                                 int targetWidth, int targetHeight,
                                 int sourceWidth, int sourceHeight) {
        if (targetWidth == sourceWidth && targetHeight == sourceHeight) {
            icon.paintIcon(progressBar, g2, x, y);
            return;
        }
        BufferedImage source = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sourceGraphics = source.createGraphics();
        icon.paintIcon(progressBar, sourceGraphics, 0, 0);
        sourceGraphics.dispose();

        @SuppressWarnings("unchecked")
        Map<RenderingHints.Key, Object> oldHints = (Map<RenderingHints.Key, Object>) g2.getRenderingHints().clone();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(source, x, y, targetWidth, targetHeight, null);
        } finally {
            g2.setRenderingHints(oldHints);
        }
    }

    private void paintFallbackGlyph(Graphics2D g2, int x, int y, int width, int height) {
        Polygon glyph = new Polygon();
        glyph.addPoint(x + width / 2, y);
        glyph.addPoint(x + width,     y + height / 2);
        glyph.addPoint(x + width / 2, y + height);
        glyph.addPoint(x,             y + height / 2);
        g2.fillPolygon(glyph);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private static LegendCharacter getSelectedCharacter() {
        return LegendSettingsState.getInstance().getSelectedCharacter();
    }

    private static Color brighten(Color color) {
        return new JBColor(new Color(
                Math.max(0, Math.min(255, Math.round(color.getRed() * (float) 1.35))),
                Math.max(0, Math.min(255, Math.round(color.getGreen() * (float) 1.35))),
                Math.max(0, Math.min(255, Math.round(color.getBlue() * (float) 1.35))),
                color.getAlpha()), new Color(
                Math.max(0, Math.min(255, Math.round(color.getRed() * (float) 1.35))),
                Math.max(0, Math.min(255, Math.round(color.getGreen() * (float) 1.35))),
                Math.max(0, Math.min(255, Math.round(color.getBlue() * (float) 1.35))),
                color.getAlpha()));
    }

    private static boolean isOdd(int value) {
        return value % 2 != 0;
    }
}
