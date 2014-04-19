/*
 Copyright 2014 Red Hat, Inc. and/or its affiliates.

 This file is part of darcy.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.redhat.darcy.ui;

/**
 * Simple view implementation that always returns true for isLoaded.
 */
public class DummyView implements View {
    private ViewContext context;

    @Override
    public View setContext(ViewContext context) {
        this.context = context;
        
        return this;
    }

    @Override
    public ViewContext getContext() {
        return context;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }
}
