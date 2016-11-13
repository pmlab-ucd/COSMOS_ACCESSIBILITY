package fu.hao.android.cosmos_accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MyService extends AccessibilityService {
    private static final String TAG = "MyAccessibility";

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "config success!");
        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
        // accessibilityServiceInfo.packageNames = PACKAGES;
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        accessibilityServiceInfo.notificationTimeout = 100;
        accessibilityServiceInfo.flags = AccessibilityServiceInfo.DEFAULT |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        setServiceInfo(accessibilityServiceInfo);
    }

    /**
     * 获取节点对象唯一的id，通过正则表达式匹配
     * AccessibilityNodeInfo@后的十六进制数字
     *
     * @param node AccessibilityNodeInfo对象
     * @return id字符串
     */
    private String getNodeId(AccessibilityNodeInfo node) {
        /* 用正则表达式匹配节点Object */
        Pattern objHashPattern = Pattern.compile("(?<=@)[0-9|a-z]+(?=;)");
        Matcher objHashMatcher = objHashPattern.matcher(node.toString());

        // AccessibilityNodeInfo必然有且只有一次匹配，因此不再作判断
        objHashMatcher.find();

        return objHashMatcher.group(0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // TODO Change to check event only if new window is created
        int eventType = event.getEventType();
        String eventText = "";
        Log.i(TAG, "==============Start====================");
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo == null) {
            return;
        }

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("hierarchy");
            doc.appendChild(rootElement);
            checkNodeInfo(rootNode, doc, rootElement);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            // FIXME Correct the path
            StreamResult result = new StreamResult(
                    new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/COSMOS/" + "test.xml"));

            // Output to console for testing
            StreamResult outResult = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("File saved!");
        } catch (ParserConfigurationException pce){
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    private void checkNodeInfo(AccessibilityNodeInfo nodeInfo, Document doc, Element element) {
        if (nodeInfo == null) {
            return;
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = nodeInfo.getChild(i);
            Element childElement = doc.createElement("node");
            element.appendChild(childElement);

            childElement.setAttribute("id", getNodeId(childNode));
            childElement.setAttribute("class", childNode.getClassName().toString());
            //childElement.setAttribute("bounds", childNode.getBoundsInScreen());
            childElement.setAttribute("selected", childNode.isSelected() ? "true" : "false");
            childElement.setAttribute("password", childNode.isPassword() ? "true" : "false");
            childElement.setAttribute("long-clickable", childNode.isLongClickable() ? "true" : "false");
            childElement.setAttribute("scrollable", childNode.isScrollable() ? "true" : "false");
            childElement.setAttribute("focused", childNode.isFocused() ? "true" : "false");
            childElement.setAttribute("focusable", childNode.isFocusable() ? "true" : "false");
            childElement.setAttribute("enabled", childNode.isEnabled() ? "true" : "false");
            childElement.setAttribute("clickable", childNode.isClickable() ? "true" : "false");
            childElement.setAttribute("checked", childNode.isChecked() ? "true" : "false");
            childElement.setAttribute("checkable", childNode.isClickable() ? "true" : "false");
            childElement.setAttribute("content-desc", childNode.getContentDescription().toString());
            childElement.setAttribute("package", childNode.getPackageName().toString());
            childElement.setAttribute("text", childNode.getText().toString());
            childElement.setAttribute("index", Integer.toString(i));

            checkNodeInfo(childNode, doc, childElement);
        }
    }


        @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
    }
}
