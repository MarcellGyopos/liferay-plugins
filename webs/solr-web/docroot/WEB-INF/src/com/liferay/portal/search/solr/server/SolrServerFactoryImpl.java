/**
 * Copyright (c) 2000-2009 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.search.solr.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrServer;

/**
 * <a href="SolrServerFactoryImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Bruno Farache
 *
 */
public class SolrServerFactoryImpl implements SolrServerFactory {

	public SolrServerFactoryImpl(Map<String, SolrServer> servers) {
		for (Map.Entry<String, SolrServer> entry : servers.entrySet()) {
			String id = entry.getKey();
			SolrServer solrServer = entry.getValue();

			_liveServers.put(id, new SolrServerWrapper(id, solrServer));
		}
	}

	public SolrServer getDeadServer(SolrServerWrapper serverWrapper) {
		synchronized (this) {
			String id = serverWrapper.getId();

			if (_deadServers.containsKey(id)) {
				return _deadServers.get(id).getServer();
			}

			return null;
		}
	}

	public List<SolrServerWrapper> getDeadServers() {
		synchronized (this) {
			return new ArrayList<SolrServerWrapper>(_deadServers.values());
		}
	}

	public SolrServer getLiveServer(SolrServerWrapper serverWrapper) {
		synchronized (this) {
			String id = serverWrapper.getId();

			if (_liveServers.containsKey(id)) {
				return _liveServers.get(id).getServer();
			}

			return null;
		}
	}

	public List<SolrServerWrapper> getLiveServers() {
		synchronized (this) {
			return new ArrayList<SolrServerWrapper>(_liveServers.values());
		}
	}

	public void killServer(SolrServerWrapper serverWrapper) {
		synchronized (this) {
			if (_deadServers.containsKey(serverWrapper.getId())) {
				return;
			}

			_deadServers.put(serverWrapper.getId(), serverWrapper);
			_liveServers.remove(serverWrapper.getId());
		}
	}

	public void startServer(SolrServerWrapper serverWrapper) {
		synchronized (this) {
			if (_liveServers.containsKey(serverWrapper.getId())) {
				return;
			}

			_deadServers.remove(serverWrapper.getId());
			_liveServers.put(serverWrapper.getId(), serverWrapper);
		}
	}

	private Map<String, SolrServerWrapper> _deadServers =
		new TreeMap<String, SolrServerWrapper>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, SolrServerWrapper> _liveServers =
		new TreeMap<String, SolrServerWrapper>(String.CASE_INSENSITIVE_ORDER);

}