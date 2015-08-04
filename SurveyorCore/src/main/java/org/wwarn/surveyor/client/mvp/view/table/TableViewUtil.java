package org.wwarn.surveyor.client.mvp.view.table;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Util class with commons methods to layout markup, currently used by CellTableViewComposite and TableViewComposite
 */
public class TableViewUtil {
    static String addHyperLink(String valueString, String hyperLinkValue) {
        return "<a target=\"_blank\" href=\""+ SafeHtmlUtils.htmlEscape(hyperLinkValue)+"\">"+SafeHtmlUtils.htmlEscapeAllowEntities(valueString)+"</a>";
    }

    static String addSpanAttribute(String valueStringRaw, String valueString) {
        return "<span class=\"tableContentHrefOrderHack\" title=\""+SafeHtmlUtils.htmlEscape(valueStringRaw)+"\">"+valueString+"</span>";
    }
}
