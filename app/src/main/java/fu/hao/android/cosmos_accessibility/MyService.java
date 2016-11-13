package fu.hao.android.cosmos_accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
        } catch (ParserConfigurationException pce){

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

            setAttribute(doc, childElement, "id", getNodeId(childNode));
            setAttribute(doc, childElement, "class", childNode.getClassName().toString());
            //setAttribute(doc, childElement, "bounds", childNode.getBoundsInScreen());
            setAttribute(doc, childElement, "selected", childNode.isSelected() ? "true" : "false");
            setAttribute(doc, childElement, "password", childNode.isPassword() ? "true" : "false");
            setAttribute(doc, childElement, "long-clickable", childNode.isLongClickable() ? "true" : "false");
            setAttribute(doc, childElement, "scrollable", childNode.isScrollable() ? "true" : "false");
            setAttribute(doc, childElement, "focused", childNode.isFocused() ? "true" : "false");
            setAttribute(doc, childElement, "focusable", childNode.isFocusable() ? "true" : "false");
            setAttribute(doc, childElement, "enabled", childNode.isEnabled() ? "true" : "false");
            setAttribute(doc, childElement, "clickable", childNode.isClickable() ? "true" : "false");
            setAttribute(doc, childElement, "checked", childNode.isChecked() ? "true" : "false");
            setAttribute(doc, childElement, "checkable", childNode.isClickable() ? "true" : "false");
            setAttribute(doc, childElement, "content-desc", childNode.getContentDescription().toString());
            setAttribute(doc, childElement, "package", childNode.getPackageName().toString());
            setAttribute(doc, childElement, "text", childNode.getText().toString());
            setAttribute(doc, childElement, "index", Integer.toString(i));

            checkNodeInfo(childNode, doc, childElement);
        }
    }

    private void setAttribute(Document doc, Element element, String attrName, String attrValue) {
        Attr attr = doc.createAttribute(attrName);
        attr.setValue(attrValue);
        element.setAttributeNode(attr);
    }

        @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
    }
}
