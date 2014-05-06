Smart Receipts Pro
==============

Overview
--------------

The code presented in this repository is what is used to actually build the Smart Receipts PRO APK file. In order to minimize the challenges of maintaining a single code base for two APK files (the free and paid versions of Smart Receipts), both point to the Smart Receipts Library project. As a result, only the top-level Activity class can be found in this repository. The rest of the code base may be found at:

- https://github.com/wbaumann/SmartReceiptsLibrary

Both versions of the Smart Receipts app can be found on the Play Store at:

- https://play.google.com/store/apps/details?id=wb.receipts
- https://play.google.com/store/apps/details?id=wb.receiptspro

License
--------------
Since this project relies on the iTextPDF library, which is licensed under the AGPL, it too is licensed under the APGL. A copy of this license can be found within this repository or at the following URL as desired:

- http://www.gnu.org/licenses/agpl.html