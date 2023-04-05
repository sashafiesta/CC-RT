package com.defthedev.ccrt.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer.QuadEmitter;
import static dan200.computercraft.client.render.RenderTypes.FULL_BRIGHT_LIGHTMAP;
import org.spongepowered.asm.mixin.Overwrite;
import com.mojang.math.Vector3f;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.Palette;
import org.spongepowered.asm.mixin.Shadow;
import javax.annotation.Nonnull;

@Mixin(FixedWidthFontRenderer.class)
public class MixinFixedWidthFontRenderer {
    /**
     * @author dan200
     * @author squiddev
     * @author DEF_THE_DEV
     */
    @Shadow(remap = false)
    @Final
    static float WIDTH;
    @Shadow(remap = false)
    @Final
    static float BACKGROUND_START;
    @Shadow(remap = false)
    @Final
    static float BACKGROUND_END;
    @Shadow(remap = false)
    @Final
    private static float Z_OFFSET;
    @Shadow(remap = false)
    private static void quad( QuadEmitter c, float x1, float y1, float x2, float y2, float z, byte[] rgba, float u1, float v1, float u2, float v2, int light ){};

    /**
     * @author dan200
     * @author squiddev
     * @author DEF_THE_DEV
     * @reason why should I write this?
     */
    @Overwrite(remap = false)
    private static void drawChar( QuadEmitter emitter, float x, float y, int index, byte[] colour, int light )
    {
        // Short circuit to avoid the common case - the texture should be blank here after all.
        if( index == '\0' || index == ' ' ) return;

        int column = index % 16; //1
        int row = index / 16; //2
        int xStart = 1 + column * (FixedWidthFontRenderer.FONT_WIDTH + 2); //9
        int yStart = 1 + row * (FixedWidthFontRenderer.FONT_HEIGHT + 2);   //23
        //
        //                                               #number
        quad(
                emitter, x, y, x + FixedWidthFontRenderer.FONT_WIDTH, y + FixedWidthFontRenderer.FONT_HEIGHT, 0.001f, colour,
                xStart / WIDTH, yStart / WIDTH, (xStart + FixedWidthFontRenderer.FONT_WIDTH) / WIDTH, (yStart + FixedWidthFontRenderer.FONT_HEIGHT) / WIDTH, light
        );
    }
    /**
     * @author dan200
     * @author squiddev
     * @author DEF_THE_DEV
     * @reason why should I write this?
     */
    @Overwrite(remap = false)
    private static void drawBackground(
            @Nonnull QuadEmitter emitter, float x, float y, @Nonnull TextBuffer backgroundColour, @Nonnull Palette palette, boolean greyscale,
            float leftMarginSize, float rightMarginSize, float height, int light
    )
    {
        if( leftMarginSize > 0 )
        {
            byte[] colourA = palette.getByteColour( FixedWidthFontRenderer.getColour( backgroundColour.charAt( 0 ), Colour.BLACK ), greyscale );
            FixedWidthFontRenderer.drawQuad( emitter, x - leftMarginSize, y, 0.0005f, leftMarginSize, height, colourA, light );
        }

        if( rightMarginSize > 0 )
        {
            byte[] colourB = palette.getByteColour( FixedWidthFontRenderer.getColour( backgroundColour.charAt( backgroundColour.length() - 1 ), Colour.BLACK ), greyscale );
            FixedWidthFontRenderer.drawQuad( emitter, x + backgroundColour.length() * FixedWidthFontRenderer.FONT_WIDTH, y, 0.0005f, rightMarginSize, height, colourB, light );
        }

        // Batch together runs of identical background cells.
        int blockStart = 0;
        char blockColour = '\0';
        for( int i = 0; i < backgroundColour.length(); i++ )
        {
            char colourIndex = backgroundColour.charAt( i );
            if( colourIndex == blockColour ) continue;

            if( blockColour != '\0' )
            {
                byte[] colourC = palette.getByteColour( FixedWidthFontRenderer.getColour( blockColour, Colour.BLACK ), greyscale );
                FixedWidthFontRenderer.drawQuad( emitter, x + blockStart * FixedWidthFontRenderer.FONT_WIDTH, y, 0.0005f, FixedWidthFontRenderer.FONT_WIDTH * (i - blockStart), height, colourC, light );
            }

            blockColour = colourIndex;
            blockStart = i;
        }

        if( blockColour != '\0' )
        {
            byte[] colourD = palette.getByteColour( FixedWidthFontRenderer.getColour( blockColour, Colour.BLACK ), greyscale );
            FixedWidthFontRenderer.drawQuad( emitter, x + blockStart * FixedWidthFontRenderer.FONT_WIDTH, y, 0.0005f, FixedWidthFontRenderer.FONT_WIDTH * (backgroundColour.length() - blockStart), height, colourD, light );
        }
    }
    /**
     * @author dan200
     * @author squiddev
     * @author DEF_THE_DEV
     * @reason why should I write this?
     */
    @Overwrite(remap = false)
    public static void drawTerminalForeground( @Nonnull QuadEmitter emitter, float x, float y, @Nonnull Terminal terminal, boolean greyscale )
    {
        Palette palette = terminal.getPalette();
        int height = terminal.getHeight();

        // The main text terminal.getBackgroundColourLine( i )
        for( int i = 0; i < height; i++ )
        {
            float rowY = y + FixedWidthFontRenderer.FONT_HEIGHT * i;
            TextBuffer textColour = terminal.getBackgroundColourLine( i );
            TextBuffer text = terminal.getLine( i );
            for( int j = 0; j < text.length(); j++ )
            {
                byte[] colour = palette.getByteColour( FixedWidthFontRenderer.getColour( textColour.charAt( j ), Colour.BLACK ), greyscale );
                colour[3] = (byte)255;
                int index = text.charAt( j );
                if( index != '\0' && index != ' ' )
                {
                    float xN = x + j * FixedWidthFontRenderer.FONT_WIDTH;
                    //                                                 #number
                    quad(
                            emitter, xN, rowY, xN + FixedWidthFontRenderer.FONT_WIDTH, rowY + FixedWidthFontRenderer.FONT_HEIGHT, 0.001f, colour,
                            200 / WIDTH, 200 / WIDTH, (200 + FixedWidthFontRenderer.FONT_WIDTH) / WIDTH, (200 + FixedWidthFontRenderer.FONT_HEIGHT) / WIDTH, FULL_BRIGHT_LIGHTMAP
                    );
                }
            }
            FixedWidthFontRenderer.drawString(
                    emitter, x, rowY, terminal.getLine( i ), terminal.getTextColourLine( i ),
                    palette, greyscale, FULL_BRIGHT_LIGHTMAP
            );
        }
    }
    /**
     * @author dan200
     * @author squiddev
     * @author DEF_THE_DEV
     * @reason why should I write this?
     */
    @Overwrite(remap = false)
    public static void drawTerminal(
            @Nonnull QuadEmitter emitter, float x, float y,
            @Nonnull Terminal terminal, boolean greyscale,
            float topMarginSize, float bottomMarginSize, float leftMarginSize, float rightMarginSize
    )
    {

        FixedWidthFontRenderer.drawTerminalBackground(
                emitter, x, y, terminal, greyscale,
                topMarginSize, bottomMarginSize, leftMarginSize, rightMarginSize
        );

        // Render the foreground with a slight offset. By calling .translate() on the matrix itself, we're translating
        // in screen space, rather than in model/view space.
        // It's definitely not perfect, but better than z fighting!
        var transformBackup = emitter.poseMatrix().copy();
        emitter.poseMatrix().translate( new Vector3f( 0, 0, Z_OFFSET ) );

        drawTerminalForeground( emitter, x, y, terminal, greyscale );
        FixedWidthFontRenderer.drawCursor( emitter, x, y, terminal, greyscale );
        float height = terminal.getHeight()*FixedWidthFontRenderer.FONT_HEIGHT+topMarginSize+bottomMarginSize;
        float width = terminal.getWidth()*FixedWidthFontRenderer.FONT_WIDTH+leftMarginSize+rightMarginSize;
        byte[] COLLINE = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 255 };
        for(int i = 0; i < height; i++)
        {
            float vi = i - topMarginSize;
            //                                                                     #number
            quad( emitter, x-leftMarginSize, y+vi, x + width - 1.8f, y + vi + 0.2f, 0.003f, COLLINE, BACKGROUND_START, BACKGROUND_START, BACKGROUND_END, BACKGROUND_END, FULL_BRIGHT_LIGHTMAP );
        }
        emitter.poseMatrix().load( transformBackup );
    }
}
