/****************************************************************************************
 * Copyright (c) 2021 Vraj Patel <vrajpatel098@gmail.com>                               *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.vrajpatel.book_keeper;

public class BookModel {

    private String title;
    private String titleLowerCase;
    private String author;
    private String shelfLocation;
    private boolean readStatus;
    private int ID;

    public BookModel(String title, String titleLowerCase, String author,
                     boolean readStatus, int ID, String shelfLocation) {
        this.title = title;
        this.titleLowerCase = titleLowerCase;
        this.author = author;
        this.readStatus = readStatus;
        this.ID = ID;
        this.shelfLocation = shelfLocation;
    }

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getTitleLowerCase() {return titleLowerCase;}
    public void setTitleLowerCase(String titleLowerCase) {this.titleLowerCase = titleLowerCase;}

    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}

    public boolean getReadStatus() {return readStatus;}
    public void setReadStatus(boolean readStatus) {this.readStatus = readStatus;}

    public String getShelfLocation() {return shelfLocation;}
    public void setShelfLocation(String shelfLocation) {this.shelfLocation = shelfLocation;}

    public int getID() {return ID;}
    public void setID(int ID) {this.ID = ID;}
}