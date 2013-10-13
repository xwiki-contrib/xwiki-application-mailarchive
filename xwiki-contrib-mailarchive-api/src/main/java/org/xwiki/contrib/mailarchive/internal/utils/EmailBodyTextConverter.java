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
package org.xwiki.contrib.mailarchive.internal.utils;

import java.io.StringReader;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * A renderer of email body. Implements icon and blockquote transformations. Renders content from HTML or plain text
 * into XHTML/1.0.
 * 
 * @version $Id$
 */
public class EmailBodyTextConverter
{

    public static final void main(String[] args) throws ComponentLookupException, ParseException,
        TransformationException
    {

        final String SOURCE =
            new StringBuilder("**This is some fake content.**\n").append("\n").append("Michel on 202020 wrote:\n")
                .append("\n").append("> something\n").append("> more things\n").append(">> even more\n")
                .append("finished\n").append(">> again\n").append(">> quoted\n").toString();

        // Initialize Rendering components and allow getting instances
        EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
        componentManager.initialize(EmailBodyTextConverter.class.getClassLoader());

        Parser parser = componentManager.getInstance(Parser.class, Syntax.XWIKI_2_1.toIdString());
        XDOM xdom = parser.parse(new StringReader(SOURCE));

        // Execute the Macro Transformation to execute Macros.
        Transformation transformation = componentManager.getInstance(Transformation.class, "icon");
        TransformationContext txContext = new TransformationContext(xdom, parser.getSyntax());
        transformation.transform(xdom, txContext);

        // Convert input in XWiki Syntax 2.1 into XHTML. The result is stored in the printer.
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer =
            componentManager.getInstance(BlockRenderer.class, new Syntax(new SyntaxType("xhtmlemail", "xhtmlemail"),
                "1.0").toIdString());
        renderer.render(xdom, printer);

        System.out.println(printer.toString());
    }

}
