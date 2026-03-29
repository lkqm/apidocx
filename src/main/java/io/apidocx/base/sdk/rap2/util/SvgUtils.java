package io.apidocx.base.sdk.rap2.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

public class SvgUtils {

    private SvgUtils() {
    }

    public static byte[] convertToPngBytes(byte[] svg) {
        return convertToImageBytes(svg, "png");
    }

    public static byte[] convertToJpegBytes(byte[] svg) {
        return convertToImageBytes(svg, "jpeg");
    }

    /**
     * 将SVG字节数组渲染为图片字节数组。
     * 使用Batik底层bridge/gvt API直接渲染到BufferedImage，再由Java原生ImageIO编码输出，
     * 完全绕开Batik的ImageWriterRegistry/ServiceLoader，避免IDEA 2025.2+中因ClassLoader
     * 隔离导致DelegatingPNGImageWriter强转失败的兼容性问题。
     */
    private static byte[] convertToImageBytes(byte[] svg, String format) {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            SVGDocument document = factory.createSVGDocument(null, new ByteArrayInputStream(svg));

            UserAgentAdapter userAgent = new UserAgentAdapter();
            BridgeContext bridgeContext = new BridgeContext(userAgent);
            GVTBuilder builder = new GVTBuilder();
            GraphicsNode rootNode = builder.build(bridgeContext, document);

            int width = (int) bridgeContext.getDocumentSize().getWidth();
            int height = (int) bridgeContext.getDocumentSize().getHeight();
            if (width <= 0) {
                width = 200;
            }
            if (height <= 0) {
                height = 80;
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, width, height);
                rootNode.paint(g2d);
            } finally {
                g2d.dispose();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, format, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("svg图片转换失败", e);
        }
    }

}
