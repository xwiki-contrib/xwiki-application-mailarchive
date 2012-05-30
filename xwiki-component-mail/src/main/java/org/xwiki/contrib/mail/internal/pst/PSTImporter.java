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
package org.xwiki.contrib.mail.internal.pst;

import java.util.Vector;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;

/**
 * @version $Id$
 */
public class PSTImporter
{
    int depth = -1;

    public PSTImporter(String filename)
    {
        try {
            PSTFile pstFile = new PSTFile(filename);
            System.out.println(pstFile.getMessageStore().getDisplayName());
            processFolder(pstFile.getRootFolder());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void processFolder(PSTFolder folder) throws PSTException, java.io.IOException
    {
        depth++;
        // the root folder doesn't have a display name
        if (depth > 0) {
            printDepth();
            System.out.println(folder.getDisplayName());
        }

        // go through the folders...
        if (folder.hasSubfolders()) {
            Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                processFolder(childFolder);
            }
        }

        // and now the emails for this folder
        if (folder.getContentCount() > 0) {
            depth++;
            PSTMessage email = (PSTMessage) folder.getNextChild();
            while (email != null) {
                printDepth();
                System.out.println("Email: " + email.getSubject());
                email = (PSTMessage) folder.getNextChild();
            }
            depth--;
        }
        depth--;
    }

    public void printDepth()
    {
        for (int x = 0; x < depth - 1; x++) {
            System.out.print(" | ");
        }
        System.out.print(" |- ");
    }
}
