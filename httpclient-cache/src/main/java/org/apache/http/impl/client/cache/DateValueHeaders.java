package org.apache.http.impl.client.cache;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;

/**
 * 日付形式の値を持つ {@link Header} 用の便利クラス。 <br/>
 * 日付形式の {@link Header} の値を {@link Date} として返したり、 効率化のためエポック秒で新たなヘッダに保存したりする。
 */
public class DateValueHeaders {

	public static final int NUMBER_OF_EPOCH_HEADER = 3;
	static final String HEADER_DATE_EPOCH = "Date-Epoch";
	static final String HEADER_EXPIRES_EPOCH = "Expires-Epoch";
	static final String HEADER_LAST_MODIFIED_EPOCH = "Last-Modified-Epoch";

	private DateValueHeaders() {
	}

	public static Date getDate(final HttpResponse response) {
		return parseDate(response, HttpHeaders.DATE,
				HEADER_DATE_EPOCH);
	}

	public static Date getExpires(final HttpResponse response) {
		return parseDate(response, HttpHeaders.EXPIRES,
				HEADER_EXPIRES_EPOCH);
	}

	public static Date getLastModified(final HttpResponse response) {
		return parseDate(response, HttpHeaders.LAST_MODIFIED,
				HEADER_LAST_MODIFIED_EPOCH);
	}

	public static Date parseDate(final HttpResponse response,
			final String headerName, final String epochHeaderName) {
		final Header epochHeader = response.getFirstHeader(epochHeaderName);
		if (epochHeader != null) {
			final String epochValue = epochHeader.getValue();
			if (epochValue == null || epochValue.length() == 0) {
				return null;
			}
			return new Date(Long.valueOf(epochHeader.getValue()));
		}

		final Header header = response.getFirstHeader(headerName);
		Date date = null;
		if (header != null) {
			try {
				date = DateUtils.parseDate(header.getValue());
			} catch (DateParseException e) {
			}
		}
		response.addHeader(epochHeaderName,
				date != null ? Long.toString(date.getTime()) : "");
		return date;
	}

	public static void assureEpochHeaders(final HttpResponse response) {
		DateValueHeaders.getDate(response);
		DateValueHeaders.getExpires(response);
		DateValueHeaders.getLastModified(response);
	}

	public static void assureEpochHeaders(final List<Header> headers) {
		assureEpochHeader(headers, HttpHeaders.DATE, HEADER_DATE_EPOCH);
		assureEpochHeader(headers, HttpHeaders.EXPIRES, HEADER_EXPIRES_EPOCH);
		assureEpochHeader(headers, HttpHeaders.LAST_MODIFIED,
				HEADER_LAST_MODIFIED_EPOCH);
	}

	static void assureEpochHeader(final List<Header> headers,
			final String headerName, final String epochHeaderName) {
		removeHeaders(headers, epochHeaderName); // HttpCacheEntry更新時に変更されることがあるので一度取り除く
		final Header header = getFirstHeader(headers, headerName);
		Date date = null;
		if (header != null) {
			try {
				date = DateUtils.parseDate(header.getValue());
			} catch (DateParseException e) {
			}
		}

		headers.add(new BasicHeader(epochHeaderName, date != null ? Long
				.toString(date.getTime()) : ""));
	}
	
	static void removeHeaders(final List<Header> headers, final String name) {
		final Iterator<Header> it = headers.iterator();
		while(it.hasNext()) {
			final Header header = it.next();
			if (header.getName().equalsIgnoreCase(name)) {
				it.remove();
			}
		}
	}

	static Header getFirstHeader(final List<Header> headers, final String name) {
		for (int i = 0; i < headers.size(); i++) {
			Header header = headers.get(i);
			if (header.getName().equalsIgnoreCase(name)) {
				return header;
			}
		}
		return null;
	}

	public static Date getDate(final HttpCacheEntry entry) {
		return parseDate(entry, HEADER_DATE_EPOCH);
	}

	public static Date getExpires(final HttpCacheEntry entry) {
		return parseDate(entry, HEADER_EXPIRES_EPOCH);
	}

	public static Date getLastModified(final HttpCacheEntry entry) {
		return parseDate(entry, HEADER_LAST_MODIFIED_EPOCH);
	}

	static Date parseDate(final HttpCacheEntry entry, final String headerName) {
		final Header epochHeader = entry.getFirstHeader(headerName);
		assert epochHeader != null;
		final String epochValue = epochHeader.getValue();
		if (epochValue == null || epochValue.length() == 0) {
			return null;
		}
		return new Date(Long.valueOf(epochValue));
	}

}
