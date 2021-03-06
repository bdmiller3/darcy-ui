/*
 Copyright 2014 Red Hat, Inc. and/or its affiliates.

 This file is part of darcy-ui.

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.redhat.darcy.ui.annotations.Context;
import com.redhat.darcy.ui.annotations.NotRequired;
import com.redhat.darcy.ui.annotations.Require;
import com.redhat.darcy.ui.annotations.RequireAll;
import com.redhat.darcy.ui.api.Locator;
import com.redhat.darcy.ui.api.ViewElement;
import com.redhat.darcy.ui.api.elements.Element;
import com.redhat.darcy.ui.api.elements.Findable;
import com.redhat.darcy.ui.testing.doubles.NeverDisplayedElement;
import com.redhat.darcy.ui.testing.doubles.NeverFoundElement;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class AbstractViewElementIsPresentTest {
    @Test(expected = NoRequiredElementsException.class)
    public void shouldThrowNoRequiredElementsExceptionIfCalledWithoutAnyAnnotatedElements() {
        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            Element element = new NeverDisplayedElement();
        };

        testView.isPresent();
    }

    @Test
    public void shouldReturnTrueIfAllRequiredFindablesArePresent() {
        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            @Require
            private Element test = new NeverDisplayedElement();
        };

        assertTrue("isPresent should return true if all required elements are present.",
                testView.isPresent());
    }

    @Test
    public void shouldReturnFalseIfNotAllRequiredElementsArePresent() {
        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            @Require
            private Element present = new NeverDisplayedElement();
            @Require
            private Element notPresent = new NeverFoundElement();
        };

        assertFalse("isPresent should return false if not all required elements are present.",
                testView.isPresent());
    }

    @Test
    public void shouldReturnTrueIfRequireAllIsUsedAndAllElementsArePresent() {
        @RequireAll
        class TestViewElement extends AbstractViewElement {
            private Element present = new NeverDisplayedElement();
            private Element present2 = new NeverDisplayedElement();

            TestViewElement(Locator parent) {
                super(parent);
            }
        }

        ViewElement testView = new TestViewElement(mock(Locator.class));

        assertTrue("isPresent should return true if all required elements are present and "
                + "RequireAll annotation is used.", testView.isPresent());
    }

    @Test
    public void shouldReturnFalseIfRequireAllIsUsedAndNotAllElementsArePresent() {
        @RequireAll class TestViewElement extends AbstractViewElement {
            private Element present = new NeverDisplayedElement();
            private Element notPresent = new NeverFoundElement();

            TestViewElement(Locator parent) {
                super(parent);
            }
        }

        ViewElement testView = new TestViewElement(mock(Locator.class));

        assertFalse("isPresent should return false if not all required elements are present " +
                "and RequireAll annotation is used.", testView.isPresent());
    }

    @Test
    public void shouldReturnTrueIfOnlyElementNotPresentIsNotRequired() {
        @RequireAll class TestViewElement extends AbstractViewElement {
            private Element present = new NeverDisplayedElement();
            @NotRequired
            private Element notPresent = new NeverFoundElement();

            TestViewElement(Locator parent) {
                super(parent);
            }
        }

        ViewElement testView = new TestViewElement(mock(Locator.class));

        assertTrue("isPresent should return true if only element not present is not required " +
                "when RequireAll annotation is used.", testView.isPresent());
    }

    @Test
    public void shouldReturnFalseIfOnlyElementPresentIsNotRequired() {
        @RequireAll class TestViewElement extends AbstractViewElement {
            @NotRequired
            private Element present = new NeverDisplayedElement();
            private Element notPresent = new NeverFoundElement();

            TestViewElement(Locator parent) {
                super(parent);
            }
        }

        ViewElement testView = new TestViewElement(mock(Locator.class));

        assertFalse("isPresent should return false if only element actually present is not " +
                "required when RequireAll annotation is used.", testView.isPresent());
    }

    @Test
    public void shouldAllowBeingOverridden() {
        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            @Require
            private Element present = new NeverDisplayedElement();

            @Override
            public boolean isPresent() {
                return super.isPresent();
            }
        };

        assertTrue(testView.isPresent());
    }

    @Test(expected = TestException.class)
    public void shouldPropagateUncheckedExceptions() {
        Element throwsExceptionOnIsPresent = mock(Element.class);
        when(throwsExceptionOnIsPresent.isPresent()).thenThrow(TestException.class);

        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            @Require Element element = throwsExceptionOnIsPresent;
        };

        testView.isPresent();
    }

    @Test
    public void shouldReturnTrueIfRequiredViewIsPresentButNotDisplayedOrLoaded() {
        ViewElement mockElement = mock(ViewElement.class);
        when(mockElement.isPresent()).thenReturn(true);
        when(mockElement.isDisplayed()).thenReturn(false);
        when(mockElement.isLoaded()).thenReturn(false);

        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            @Require
            ViewElement element = mockElement;
        };

        assertTrue("Expected ViewElement to be present due to single required field that is a " +
                "present view.", testView.isPresent());
    }

    // This scenario doesn't make any sense, but serves to prove we're not looking at isLoaded for
    // isPresent.
    @Test
    public void shouldReturnFalseIfRequiredViewIsNotPresentButIsLoadedAndDisplayed() {
        ViewElement mockElement = mock(ViewElement.class);
        when(mockElement.isDisplayed()).thenReturn(true);
        when(mockElement.isLoaded()).thenReturn(true);
        when(mockElement.isPresent()).thenReturn(false);

        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            @Require
            ViewElement element = mockElement;
        };

        assertFalse("Expected ViewElement to not be present due to single required field that " +
                "is a not present view.", testView.isPresent());
    }

    @Test
    public void shouldReturnTrueIfRequiredFindableIsPresent() {
        Findable mockFindable = mock(Findable.class);
        when(mockFindable.isPresent()).thenReturn(true);

        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            @Require
            Findable findable = mockFindable;
        };

        assertTrue("Expected ViewElement to be present due to single required field that is a " +
                "present findable.", testView.isPresent());
    }

    @Test
    public void shouldReturnFalseIfRequiredFindableIsNotPresent() {
        Findable mockFindable = mock(Findable.class);
        when(mockFindable.isPresent()).thenReturn(false);

        ViewElement testView = new AbstractViewElement(mock(Locator.class)) {
            @Require
            Findable findable = mockFindable;
        };

        assertFalse("Expected ViewElement to not be present due to single required field that is " +
                "a " +
                "non-present findable.", testView.isPresent());
    }

    @Test(expected = NoRequiredElementsException.class)
    public void shouldIgnoreFieldsAnnotatedWithContext() {
        @RequireAll class TestViewElement extends AbstractViewElement {
            @Context
            Findable findable = mock(Findable.class);

            public TestViewElement() {
                super(mock(Locator.class));
            }
        };

        TestViewElement testView = new TestViewElement();
        testView.isPresent();
    }

    @Test(expected = NoRequiredElementsException.class)
    public void shouldNotConsiderIrrelevantFieldsAsAbleToBeRequired() {
        @RequireAll class TestViewElement extends AbstractViewElement {
            String aString;

            public TestViewElement() {
                super(mock(Locator.class));
            }
        };

        TestViewElement testView = new TestViewElement();
        testView.isPresent();
    }

    @Ignore("Broken until requiring Lists is implemented, see " +
            "https://github.com/darcy-framework/darcy-ui/issues/13")
    @Test(expected = NoRequiredElementsException.class)
    public void shouldReconsiderEmptyConditionsListIfFieldIsAmbiguous() {
        @RequireAll class TestViewElement extends AbstractViewElement {
            List<String> stringList;

            public TestViewElement() {
                super(mock(Locator.class));
            }
        };

        TestViewElement testView = new TestViewElement();
        testView.isPresent();
    }

    class TestException extends RuntimeException {}
}
