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
package org.xwiki.contrib.mail.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

/**
 * This class allows reverse mapping from a mime-type to a collection of file extensions. the first file extension is
 * considered to be the preferred one.
 * <p>
 * <b>MIME types file search order:</b>
 * </p>
 * <ol>
 * <li>The file or resources named META-INF/mime.types.</li>
 * <li>The file or resource named META-INF/mimetypes.default (usually found only in the activation.jar file).</li>
 * </ol>
 * 
 * @see MimetypesFileTypeMap
 * @version $Id$
 */
public class MimetypesFileExtensionsMap
{

    public static final String DEFAULT_FILE_EXTENSION = "bin";

    private static MimetypesFileExtensionsMap singleton;

    private HashMap<String, List<String>> mimetypes;

    private MimetypesFileTypeMap activationMimetypes;

    public static MimetypesFileExtensionsMap get()
    {
        if (singleton == null) {
            singleton = new MimetypesFileExtensionsMap();
        }
        return singleton;
    }

    public String getPreferedFileExtension(String mimetype)
    {
        if (mimetype == null)
            return DEFAULT_FILE_EXTENSION;

        Collection<String> exts = mimetypes.get(mimetype);
        if (exts.size() == 0)
            return DEFAULT_FILE_EXTENSION;
        else
            return exts.iterator().next();
    }

    public boolean containsMimetype(String mimetype)
    {
        return mimetypes.containsKey(mimetype);
    }

    public Collection<String> getFileExtensions(String mimetype)
    {
        return mimetypes.get(mimetype);
    }

    public String getMimeType(String filename)
    {
        return activationMimetypes.getContentType(filename);
    }

    public String getMimeType(File file)
    {
        return activationMimetypes.getContentType(file);
    }

    private MimetypesFileExtensionsMap()
    {
        // MimetypesFileTypeMap
        mimetypes = new HashMap<String, List<String>>();
        initFromStream(ClassLoader.getSystemResourceAsStream("META-INF/mime.types"));
        initFromStream(ClassLoader.getSystemResourceAsStream("META-INF/mimetypes.default"));
        activationMimetypes = new MimetypesFileTypeMap();
    }

    private void initFromStream(InputStream in)
    {
        if (in == null)
            return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") == false) {
                    final String[] entries = line.split("\\s+");
                    if (entries != null && entries.length > 1) {
                        List<String> extensions = new ArrayList<String>();
                        for (int i = 1; i < entries.length; i++) {
                            extensions.add(entries[i]);
                        }
                        mimetypes.put(entries[0], extensions);
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
