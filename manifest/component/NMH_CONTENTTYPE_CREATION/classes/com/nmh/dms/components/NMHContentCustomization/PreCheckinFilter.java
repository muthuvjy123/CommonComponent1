/* **************************************************************************** 
 File Name:	PreCheckinFilter.java
 Creation Date:	15-04-2010
 Author:  Sapient\Mukesh Thekkumbadan
 Description:  Filter class for setting account on content
 
 Revision:  1.1
 ------------------------------------------------------------------------------
 Copyright (c) 2010, For NMInteractive. All Rights Reserved.
 =============================================================================
 Modification Log
 ----------------
 Date             Modified By			Comments
 -----------      --------------	   --------------
 20-03-2010     Mukesh Thekkumbadan         Created New File
 04-03-2011		Raquel Specketer			Added CHECKIN_ARCHIVE service to the exclusion list.
 28-03-2011		Raquel Specketer			Added IMPORT_ARCHIVE service to the exclusion list.  Fixed duplicated file extensions.
 12-04-2011		Raquel Specketer			Modified file rename process so WebCenter checkins didn't duplicate extension but other files get renamed.
 
 *************************************************************************** */
package com.nmh.dms.components.NMHContentCustomization;

import intradoc.common.ExecutionContext;
import intradoc.common.Log;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.shared.FilterImplementor;
import intradoc.shared.SharedObjects;

/**
 * 
 * 
 * @author Sapient\Mukesh
 */
public class PreCheckinFilter implements FilterImplementor
{

    public static String IDC_SERVICE = "IdcService";

    public static String DATA_FILE_FIELD_VALUE = "Data File";
    
    /**
     * 
     * 
     * 
     * 
     * @param workspace
     *            This will workspace details
     * @param dataBinder
     *            This will contains binder values
     * @param arg2
     *            of type ExecutionContext
     * @throws DataException
     *             this exception will be thrown when DB related error occcurs
     * @throws ServiceException
     *             this exception will be thrown when the service fails
     * @return continue .
     */
    public int doFilter(Workspace workspace, DataBinder dataBinder,
            ExecutionContext arg2) throws DataException, ServiceException
    {

		String serviceName = dataBinder.getLocal(IDC_SERVICE);
        // set the  dDocTitle as the filename.
		//System.out.println("IDC_SERVICE="+ serviceName);
        if (serviceName != null)
        {
                        
            if (serviceName.equalsIgnoreCase(
                    "UPDATE_DOCINFO_BYFORM")
                || serviceName.equalsIgnoreCase(
                        "CHECKIN_NEW_FOLIO")
                || serviceName.equalsIgnoreCase(
                        "UPDATE_FOLIO")
                || serviceName.equalsIgnoreCase(
                        "UNLOCK_FOLIO")
                || serviceName.equalsIgnoreCase(
                        "CREATE_FOLIO_SNAPSHOT")
                || serviceName.equalsIgnoreCase(
                        "LOCK_FOLIO")
                || serviceName.equalsIgnoreCase(
                        "CHECKIN_ARCHIVE")  /*called from batchloader*/
                || serviceName.equalsIgnoreCase(
                        "IMPORT_ARCHIVE")  /*called from the archiver import action*/)
            {
                System.out.println("Skipping checkin filter");
            }
            else
            {
                setFileName(dataBinder);
            }
        }

        return CONTINUE;
    }

    /**
     * This method is to set the fileName as title of the document to be checked
     * in.
     * 
     * @param dataBinder
     *            contains all binder values.
     */
    protected void setFileName(DataBinder dataBinder)
    {
        String isWorkFlow = "False";
        String docType = "";
        String websiteObjectType = "";
        String videoFormat = SharedObjects.getEnvironmentValue("VIDEO_FORMAT");
        String fileName = dataBinder.getLocal("primaryFile");
      
        if (fileName != null && !"".equals(fileName)
            && !"null".equals(fileName))
        {
            String as[];
            int j = (as = videoFormat.split(";")).length;
            for (int i = 0; i < j; i++)
            {
                String extension = as[i];
                if (fileName.substring(fileName.lastIndexOf(".") + 1)
                        .equalsIgnoreCase(extension))
                    isWorkFlow = "True";
            }

            dataBinder.putLocal("xNMHIsWorkFlow", isWorkFlow);
			// If the title of the doc is the same as the filename, this is either an uploads from Web Center or possibly WebDAV
			// then we won't want to duplicate the extension. 
			String docTitle = dataBinder.getLocal("dDocTitle");
			if (!fileName.equalsIgnoreCase(docTitle))
			{
				//Rename the file so the file name is a more friendly name for the folder views.
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(docTitle);
				int extPosition = fileName.lastIndexOf(".");
				if (extPosition >= 0)
				{
					stringBuffer.append(fileName.substring(extPosition));
				}
				dataBinder.putLocal("primaryFile", stringBuffer.toString());
			}
       }
        docType = dataBinder.getLocal("dDocType");
        websiteObjectType = dataBinder.getLocal("xWebsiteObjectType");

        if (docType.equalsIgnoreCase("WCMWebAsset")
            && fileName != null
            && (websiteObjectType.equals(DATA_FILE_FIELD_VALUE)))
        {
            String docTitle = dataBinder.getLocal("dDocTitle");
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(docTitle);
            stringBuffer.append(".xml");
            dataBinder.putLocal("primaryFile", stringBuffer.toString());
       }
    }
}
