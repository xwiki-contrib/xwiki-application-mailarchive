/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.mailarchive.internal.renderer;

import java.util.Map;

import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.internal.renderer.xwiki21.XWikiSyntaxChainingRenderer;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener.Event;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;

/**
 * Convert listener events to XWiki Syntax 2.0 output.
 * 
 * @version $Id$
 * @since 1.8RC1
 */
public class XHTMLEmailChainingRenderer extends XWikiSyntaxChainingRenderer
{

    // used to print the parts that need to be formatted as XHTML (urls, quotes, icons)
    private XHTMLWikiPrinter xhtmlWikiPrinter;

    public XHTMLEmailChainingRenderer(XHTMLLinkRenderer xhtmlLinkRenderer, XHTMLImageRenderer imageRenderer,
        ListenerChain chain)
    {
        super(chain, null, null);
    }

    protected XHTMLWikiPrinter getXHTMLWikiPrinter()
    {
        if (this.xhtmlWikiPrinter == null) {
            this.xhtmlWikiPrinter = new XHTMLWikiPrinter(getPrinter());
        }
        return this.xhtmlWikiPrinter;
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        if (getBlockState().isInQuotationLine()) {
            getXHTMLWikiPrinter().printXMLEndElement("p");
        }

        getXHTMLWikiPrinter().printXMLStartElement("blockquote", parameters);

        getXHTMLWikiPrinter().printXMLStartElement("p");
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement("p");

        getXHTMLWikiPrinter().printXMLEndElement("blockquote");

        if (getBlockState().isInQuotationLine()) {
            getXHTMLWikiPrinter().printXMLStartElement("p");
        }
    }

    @Override
    public void beginQuotationLine()
    {
        // Send a new line if the previous event was endQuotationLine since we need to separate each quotation line
        // or they'll printed next to each other and not on a new line each.
        if (getBlockState().isInQuotation() && getBlockState().getPreviousEvent() == Event.QUOTATION_LINE) {
            onNewLine();
        }
    }

    protected BlockStateChainingListener getBlockState()
    {
        return (BlockStateChainingListener) getListenerChain().getListener(BlockStateChainingListener.class);
    }

}
