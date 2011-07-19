/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.speedtracer.client.visualizations.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.graphics.client.Color;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.topspin.ui.client.ClickEvent;
import com.google.gwt.topspin.ui.client.ClickListener;
import com.google.gwt.topspin.ui.client.Container;
import com.google.gwt.topspin.ui.client.ContainerImpl;
import com.google.gwt.topspin.ui.client.CssTransitionEvent;
import com.google.gwt.topspin.ui.client.CssTransitionListener;
import com.google.gwt.topspin.ui.client.DefaultContainerImpl;
import com.google.gwt.topspin.ui.client.Table;
import com.google.speedtracer.client.ClientConfig;
import com.google.speedtracer.client.Logging;
import com.google.speedtracer.client.ServerEventController;
import com.google.speedtracer.client.model.NetworkResource;
import com.google.speedtracer.client.model.ServerEvent;
import com.google.speedtracer.client.model.UiEvent;
import com.google.speedtracer.client.model.NetworkResource.HeaderMap;
import com.google.speedtracer.client.model.NetworkResource.HeaderMap.IterationCallBack;
import com.google.speedtracer.client.util.TimeStampFormatter;
import com.google.speedtracer.client.util.dom.DocumentExt;
import com.google.speedtracer.client.util.dom.LazilyCreateableElement;
import com.google.speedtracer.client.util.dom.ManagesEventListeners;
import com.google.speedtracer.client.visualizations.view.Tree.ExpansionChangeListener;
import com.google.speedtracer.client.visualizations.view.Tree.Item;

import java.util.ArrayList;

/**
 * The panel of details that displays the details of the network request and
 * conditionally, the hintlet records associated with this resource.
 */
public class RequestDetails extends LazilyCreateableElement {
  /**
   * CSS.
   */
  public interface Css extends CssResource {
    String details();

    int detailsTableWidth();

    int hintletTreeMargin();

    String hintletTreeWrapper();

    String nameCell();

    String nameValueTable();

    String rowEven();

    String rowFail();

    String rowRedirect();

    String sectionHeader();

    String serverTraceTree();

    String springInsightLink();

    String springInsightViews();

    String valueCell();
  }

  /**
   * Externalized interface.
   */
  public interface Resources extends HintletRecordsTree.Resources {
    @Source("resources/RequestDetails.css")
    RequestDetails.Css requestDetailsCss();
  }

  private static class OddEvenIterator {
    private boolean even = true;

    public boolean next() {
      return even = !even;
    }
  }

  /**
   * A general controller class to support the server event tree.
   */
  private class ServerEventTreeController implements LazyEventTree.Presenter,
      Tree.SelectionChangeListener, Tree.ExpansionChangeListener {
    private static final int SIGNIFICANCE_IN_PIXELS = 1;

    public Color getColor(UiEvent event) {
      return ServerEventColors.getColorFor(event.<ServerEvent> cast());
    }

    public Color getDominantTypeColor(UiEvent event) {
      return null;
    }

    public double getInsignificanceThreshold(double msPerPixel) {
      return SIGNIFICANCE_IN_PIXELS / msPerPixel;
    }

    public String getLabel(UiEvent event) {
      assert event.getType() == ServerEvent.TYPE;
      final String label = event.<ServerEvent> cast().getServerEventData().getLabel();
      return label.length() > 50 ? label.substring(0, 50) + "..." : label;
    }

    public boolean hasDominantType(UiEvent event, UiEvent rootEvent,
        double msPerPixel) {
      // TODO(knorton): This presenter does not have dominant type color
      // overrides. Will add this as needed.
      return false;
    }

    public void onExpansionChange(Item changedItem) {
      fixHeightOfParentRow();
    }

    public void onSelectionChange(ArrayList<Item> selected) {
      fixHeightOfParentRow();
    }
  }

  /**
   * Appends a TableRowElement and populates it with two cells.
   * 
   * @param summaryTable
   * @param title
   * @param value
   */
  private static void addRowPair(Table dataTable, Css css, boolean isEven,
      String title, String value) {
    TableRowElement row = dataTable.appendRow();
    if (isEven) {
      row.setClassName(css.rowEven());
    }

    final TableCellElement nameCell = row.insertCell(-1);
    nameCell.setClassName(css.nameCell());
    nameCell.setInnerText(title);

    final TableCellElement valueCell = row.insertCell(-1);
    valueCell.setClassName(css.valueCell());
    valueCell.setInnerText(value);
  }

  /**
   * Appends a TableRowElement and populates it with two cells.
   * 
   * @param summaryTable
   * @param title
   * @param value
   */
  private static void addRowPairByClass(Table dataTable, Css css,
      String rowClass, String title, String value) {
    TableRowElement row = dataTable.appendRow();
    row.setClassName(rowClass);

    final TableCellElement nameCell = row.insertCell(-1);
    nameCell.setClassName(css.nameCell());
    nameCell.setInnerText(title);

    final TableCellElement valueCell = row.insertCell(-1);
    valueCell.setClassName(css.valueCell());
    valueCell.setInnerText(value);
  }

  private static AnchorElement createNewTabLink(Document document,
      String className, String href, String text) {
    final AnchorElement link = document.createAnchorElement();
    link.setClassName(className);
    link.setHref(href);
    link.setInnerText(text);
    link.setTarget("_blank");
    return link;
  }

  private static Element createSectionHeader(Css css, Document document,
      String text) {
    final DivElement header = document.createDivElement();
    header.setClassName(css.sectionHeader());
    header.setInnerHTML(text);
    return header;
  }

  private static Table createTable(Container container, String classname) {
    final Table table = new Table(container);
    table.getElement().setClassName(classname);
    return table;
  }

  private static void fillHeaderTable(final Table headerTable, final Css css,
      HeaderMap headers) {
    if (headers != null) {
      headers.iterate(new IterationCallBack() {
        final OddEvenIterator iter = new OddEvenIterator();

        public void onIteration(String key, String value) {
          addRowPair(headerTable, css, iter.next(), key, value);
        }
      });
    }
  }

  /**
   * Produces a commonly used string in this part of the UI "@23ms for 112ms".
   * 
   * @param startTime
   * @param endTime
   * @return
   */
  private static String formatTimeSpan(double startTime, double endTime) {
    return "@" + TimeStampFormatter.formatMilliseconds(startTime) + " for "
        + TimeStampFormatter.formatMilliseconds(endTime - startTime);
  }

  private static void maybeAddSpringInsightLinks(ServerEvent event, Css css,
      Element parent) {
    final ServerEvent.Data data = event.getServerEventData();
    final String applicationUrl = data.getApplicationUrl();
    final String endPointUrl = data.getEndPointUrl();
    final String traceViewUrl = data.getTraceViewUrl();
    if (applicationUrl == null && endPointUrl == null && traceViewUrl == null) {
      return;
    }

    final Document document = parent.getOwnerDocument();
    final DivElement element = document.createDivElement();
    element.setClassName(css.springInsightViews());
    element.setInnerText("Spring Insight Views: ");

    element.appendChild(document.createTextNode("("));

    // Add Trace URL
    if (traceViewUrl != null) {
      element.appendChild(createNewTabLink(document, css.springInsightLink(),
          traceViewUrl, "Trace"));
    }

    // Add EndPoint URL
    if (endPointUrl != null) {
      // Is there a previous item?
      if (traceViewUrl != null) {
        element.appendChild(document.createTextNode(", "));
      }
      element.appendChild(createNewTabLink(document, css.springInsightLink(),
          endPointUrl, "EndPoint"));
    }

    // Add Application URL
    if (applicationUrl != null) {
      // Is there a previous item?
      if (traceViewUrl != null || endPointUrl != null) {
        element.appendChild(document.createTextNode(", "));
      }
      element.appendChild(createNewTabLink(document, css.springInsightLink(),
          applicationUrl, "Application"));
    }

    element.appendChild(document.createTextNode(") "));
    parent.appendChild(element);
  }

  private Element contentElem;

  private HintletRecordsTree hintletTree;

  private DivElement hintletTreeWrapper;

  private NetworkResource info;

  private boolean isVisible = false;

  private final Element parentElem;

  /**
   * We need a reference to the styles for our parent Widget because we have
   * some toggles in this class that mutate the styles of the
   * {@link NetworkPillBox}.
   */
  private final NetworkPillBox.Css pbCss;

  private final Resources resources;

  private final ServerEventController serverEventController;

  /**
   * ctor.
   * 
   * @param parentElem what we attach to
   * @param networkResource the information about this network request/response
   * @param listenerManager a manager for our event listeners
   * @param resources {@link NetworkPillBox.Resources} that contains relevant
   *          images and Css.
   */
  public RequestDetails(Element parentElem, NetworkResource networkResource,
      ManagesEventListeners listenerManager,
      NetworkPillBox.Resources resources,
      ServerEventController serverEventController) {
    super(listenerManager, resources.requestDetailsCss().details());
    this.parentElem = parentElem;
    this.resources = resources;
    this.pbCss = resources.networkPillBoxCss();
    this.info = networkResource;
    this.serverEventController = serverEventController;
  }

  public void refresh() {
    // The hintlet information might have changed since the details panel
    // was first created.
    if (hintletTreeWrapper != null && info.getHintRecords() != null) {
      if (hintletTree == null) {
        createHintletTree();
      } else {
        hintletTree.refresh(info.getHintRecords());
      }
    }

    // Update the content if it's visible.
    if (isVisible) {
      populateContent();
    }
  }

  /**
   * Toggles the Visibility of the RequestDetails.
   * 
   * @return whether or not we closed or opened the details
   */
  public boolean toggleVisibility() {
    if (isVisible) {
      getElement().getStyle().setHeight(0, Unit.PX);
      isVisible = false;
    } else {
      if (contentElem == null) {
        // Lazily initialize contentElem
        contentElem = Document.get().createDivElement();
        // Fill the contentElem
        populateContent();
        getElement().appendChild(contentElem);
      }

      // Make the element renderable
      getElement().getStyle().setProperty("display", "block");
      fixHeightOfParentRow();
      parentElem.addClassName(pbCss.selected());
      isVisible = true;
    }
    return isVisible;
  }

  /**
   * Updates the Details of the Network Request.
   * 
   * @param info NetworkResource JSO containing the information about the
   *          request/response for the resource
   */
  public void updateInfo(NetworkResource info) {
    this.info = info;
    populateContent();
  }

  @Override
  protected Element createElement() {
    final Element elem = Document.get().createDivElement();
    // CallBack invoked after collapsing RequestDetails
    manageEventListener(CssTransitionEvent.addTransitionListener(this, elem,
        new CssTransitionListener() {
          public void onTransitionEnd(CssTransitionEvent event) {
            if (!isVisible) {
              elem.getStyle().setProperty("display", "none");
              parentElem.removeClassName(RequestDetails.this.pbCss.selected());
            }
          }
        }));

    // We want to stop the annoying issue of clicking inside the details view
    // collapsing the expansion.
    manageEventListener(ClickEvent.addClickListener(this, elem,
        new ClickListener() {
          public void onClick(ClickEvent event) {
            event.getNativeEvent().cancelBubble(true);
          }
        }));

    parentElem.appendChild(elem);
    return elem;
  }

  private void createHintletTree() {
    hintletTree = new HintletRecordsTree(new DefaultContainerImpl(
        hintletTreeWrapper), info.getHintRecords(), resources);

    // Hook listener to tree list to monitor expansion changes.
    hintletTree.addExpansionChangeListener(new ExpansionChangeListener() {
      public void onExpansionChange(Item changedItem) {
        fixHeightOfParentRow();
      }
    });

    // We make sure to have the tree cleaned up when we clean up ourselves.
    manageEventListener(hintletTree.getRemover());
  }

  /**
   * Fills in the Summary Data.
   * 
   * @param summaryTable
   * @param info
   */
  private void fillSummaryTable(Table summaryTable, Css css,
      NetworkResource info) {
    final OddEvenIterator iter = new OddEvenIterator();

    if (ClientConfig.isDebugMode()) {
      addRowPair(summaryTable, css, iter.next(), "Identifier",
          info.getIdentifier() + "");
    }
    addRowPair(summaryTable, css, iter.next(), "URL", info.getUrl());

    addRowPair(summaryTable, css, iter.next(), "From Cache", info.isCached()
        + "");
    addRowPair(summaryTable, css, iter.next(), "Method", info.getHttpMethod());

    if (info.didFail()) {
      addRowPairByClass(summaryTable, css, css.rowFail(), "Http Status",
          info.formatHttpStatus());
    } else if (info.isRedirect()) {
      addRowPairByClass(summaryTable, css, css.rowRedirect(), "Http Status",
          info.formatHttpStatus());
    } else {
      addRowPair(summaryTable, css, iter.next(), "Http Status",
          info.formatHttpStatus());
    }

    addRowPair(summaryTable, css, iter.next(), "Mime-type", info.getMimeType());

    String requestTiming, responseTiming, totalTiming;

    if (info.didFail()) {
      requestTiming = formatTimeSpan(info.getStartTime(), info.getEndTime());
      responseTiming = "No response";
      totalTiming = formatTimeSpan(info.getStartTime(), info.getEndTime())
          + " with an error";
    } else {
      requestTiming = formatTimeSpan(info.getStartTime(),
          info.getResponseReceivedTime());
      responseTiming = formatTimeSpan(info.getResponseReceivedTime(),
          info.getEndTime());
      totalTiming = formatTimeSpan(info.getStartTime(), info.getEndTime());
    }

    addRowPair(summaryTable, css, iter.next(), "Total Bytes",
        getContentLengthString(info));
    addRowPair(summaryTable, css, iter.next(), "Request Timing", requestTiming);
    addRowPair(summaryTable, css, iter.next(), "Response Timing",
        responseTiming);
    addRowPair(summaryTable, css, iter.next(), "Total Timing", totalTiming);

    // Detailed Timing Breakdown
    if (info.hasDetailedTiming()) {
      addRowPair(summaryTable, css, iter.next(), "Blocked",
          TimeStampFormatter.formatMilliseconds(Math.max(info.getRequestTime()
              - info.getStartTime(), 0)));
      if (info.getDnsDuration() >= 0) {
        addRowPair(summaryTable, css, iter.next(), "DNS Resolution",
            TimeStampFormatter.formatMilliseconds(info.getDnsDuration()));
      } else {
        addRowPair(summaryTable, css, iter.next(), "DNS Resolution",
            TimeStampFormatter.formatMilliseconds(0) + " (Cached DNS)");
      }
      if (info.getConnectDuration() >= 0) {
        addRowPair(summaryTable, css, iter.next(), "Connecting",
            TimeStampFormatter.formatMilliseconds(info.getOnlyConnectDuration()));
      } else {
        addRowPair(summaryTable, css, iter.next(), "Connecting",
            TimeStampFormatter.formatMilliseconds(0) + " (Reused)");
      }
      addRowPair(summaryTable, css, iter.next(), "Sending Request",
          TimeStampFormatter.formatMilliseconds(info.getSendDuration()));
    
      // Only show Proxy or SSL if they apply
      if (info.getProxyDuration() >= 0) {
        addRowPair(summaryTable, css, iter.next(), "Proxy",
            TimeStampFormatter.formatMilliseconds(info.getProxyDuration()));
      }
      if (info.getSslDuration() >= 0) {
        addRowPair(summaryTable, css, iter.next(), "SSL Handshake",
            TimeStampFormatter.formatMilliseconds(info.getSslDuration()));
      }
    }
  }

  /**
   * When the tree expands, the bottom of the details view slides under the next
   * row. This will adjust the height of the details view to accommodate growth
   * or shrinking.
   */
  private void fixHeightOfParentRow() {
    getElement().getStyle().setPropertyPx("height",
        contentElem.getOffsetHeight());
  }

  /**
   * Looks up the content length in bytes and returns the stringified version
   * for display purposes.
   * 
   * @param info The {@link NetworkResource} that contains the info for the
   *          request.
   * @return returns the content length as a String.
   */
  private String getContentLengthString(NetworkResource info) {
    int contentLength = info.getDataLength();
    return ((contentLength < 0) ? "" : contentLength + " bytes");
  }

  private void maybeShowServerEvents(final Element parent,
      final Element insertAfter, final Css css, final Document document) {
    if (!info.hasServerTraceUrl()) {
      return;
    }

    final String traceUrl = info.getServerTraceUrl();

    // TODO(knorton): When playing back from a dump, we do not want to try to
    // fetch the server-side trace.
    serverEventController.requestTraceFor(info,
        new ServerEventController.RequestTraceCallback() {
          public void onFailure() {
            if (ClientConfig.isDebugMode()) {
              Logging.getLogger().logText(
                  "Failed to fetch server trace: " + traceUrl);
            }
          }

          public void onSuccess(ServerEvent event) {
            // insertBefore may be null, in which case Element.insertBefore will
            // append.
            final Element insertBefore = insertAfter.getNextSiblingElement();

            parent.insertBefore(createSectionHeader(css, document,
                "Server Trace"), insertBefore);

            final DivElement container = document.createDivElement();
            container.setClassName(css.serverTraceTree());
            parent.insertBefore(container, insertBefore);

            // TODO(knorton): Spring Insight specific functionality that needs
            // to be better generalized.
            maybeAddSpringInsightLinks(event, css, container);

            final LazyEventTree.Resources treeResources = GWT.create(LazyEventTree.Resources.class);
            final ServerEventTreeController controller = new ServerEventTreeController();

            final LazyEventTree tree = new LazyEventTree(
                new DefaultContainerImpl(container), controller, event,
                new EventTraceBreakdown(event, controller, treeResources),
                treeResources);
            tree.addSelectionChangeListener(controller);
            tree.addExpansionChangeListener(controller);
            fixHeightOfParentRow();
          }
        });
  }

  /**
   * Populates the contentElement with the info.
   * 
   * Should only be invoked after contentElem is initialized.
   */
  private void populateContent() {
    // We may be doing an update. So we blow away the existing content.
    contentElem.setInnerHTML("");
    final Document document = contentElem.getOwnerDocument();

    ContainerImpl container = new DefaultContainerImpl(contentElem);
    final Css css = resources.requestDetailsCss();
    hintletTreeWrapper = DocumentExt.get().createDivWithClassName(
        css.hintletTreeWrapper());
    contentElem.appendChild(hintletTreeWrapper);
    if (info.getHintRecords() != null) {
      createHintletTree();
    }

    // Summary Table.
    contentElem.appendChild(createSectionHeader(css, document, "Summary"));
    fillSummaryTable(createTable(container, css.nameValueTable()), css, info);

    // Request Headers.
    contentElem.appendChild(createSectionHeader(css, document,
        "Request Headers"));
    fillHeaderTable(createTable(container, css.nameValueTable()), css,
        info.getRequestHeaders());

    // Response Headers.
    contentElem.appendChild(createSectionHeader(css, document,
        "Response Headers"));
    fillHeaderTable(createTable(container, css.nameValueTable()), css,
        info.getResponseHeaders());

    // Server Events.
    maybeShowServerEvents(contentElem, hintletTreeWrapper, css, document);
  }
}
