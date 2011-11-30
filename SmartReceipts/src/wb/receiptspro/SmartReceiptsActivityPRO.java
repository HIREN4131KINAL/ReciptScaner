/*
 * Copyright (C) 2011 Will Baumann

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package wb.receiptspro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.RelativeLayout;
import wb.receiptslibrary.SmartReceiptsActivity;

/**
 * TODO:
 * 	1. - Content Provider
 * 	2. - (+) Fix null dates (don't all people to tab into the date field)
 * 	3. - (+) Look into SD Card Errors --> Likely a singular issue. Check to see how many other Nexus S downloads there are
 * 	4. - (+) Image Callback -> If it failed to save, remove image filename (don't do second Toast... failed to save -> image successfully added to x)
 * 	5. - (+) Close the databases in addition to the cursor (not sure if after every getSQLiteDB or at onPause)
 *  6. - (+) Catch Camera exception when the camera is already open
 *  7. - Add Internal Storage (do this to MyCameraActivity as well)
 */

public class SmartReceiptsActivityPRO extends SmartReceiptsActivity{
    
    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        this.setContentView(R.layout.main);
	    RelativeLayout mainLayout = (RelativeLayout) this.findViewById(R.id.main_layout);
        ListView listView = (ListView) this.findViewById(R.id.listview);
        super.onCreate(savedInstanceState, mainLayout, listView);
        Intent intent = new Intent();
        intent.setAction(FILTER_ACTION);
        sendBroadcast(intent);
    }

	@Override
	public String getPackageName() {
		return this.getClass().getPackage().getName().toString();
	}
    
}