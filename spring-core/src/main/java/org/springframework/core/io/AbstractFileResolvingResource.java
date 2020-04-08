/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

import org.springframework.util.ResourceUtils;

/**
 * Abstract base class for resources which resolve URLs into File references,
 * such as {@link UrlResource} or {@link ClassPathResource}.
 *
 * <p>Detects the "file" protocol as well as the JBoss "vfs" protocol in URLs,
 * resolving file system references accordingly.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
// 抽象File资源操作抽象类
public abstract class AbstractFileResolvingResource extends AbstractResource {// resolve：解析

	// 是否存在
	@Override
	public boolean exists() {
		try {
			// 获取URL
			URL url = getURL();
			// 1.文件路径，由文件系统解析
			if (ResourceUtils.isFileURL(url)) {
				// Proceed with file system resolution
				return getFile().exists();
			}
			else {
				// 2.否则，采用尝试获取URL链接请求中header的content-length属性
				// Try a URL connection content-length header
				URLConnection con = url.openConnection();
				customizeConnection(con);
				HttpURLConnection httpCon =
						(con instanceof HttpURLConnection ? (HttpURLConnection) con : null);
				// 2.1 httpCon对象非空，尝试获取链接，并获取返回状态码
				if (httpCon != null) {
					// 获取状态码
					int code = httpCon.getResponseCode();
					if (code == HttpURLConnection.HTTP_OK) {// 链接成功
						return true;
					}
					else if (code == HttpURLConnection.HTTP_NOT_FOUND) {// 未找到
						return false;
					}
				}
				// 2.2 链接失败，获取返content-length的值是否大于等于0
				if (con.getContentLengthLong() > 0) {
					return true;
				}
				// 2.3 httpCon对象非空，但HTTP状态码不是OK／NOT_FOUND状态
				if (httpCon != null) {
					// No HTTP OK status, and no content-length header: give up
					// 没有HTTP OK状态，也没有获取到content-length header，则放弃链接，返回false
					httpCon.disconnect();
					return false;
				}
				else {
					// Fall back to stream existence: can we open the stream?
					// 2.4 尝试获取文件流,成功，则存在
					getInputStream().close();
					return true;
				}
			}
		}
		catch (IOException ex) {
			return false;
		}
	}

	// 是否可读
	@Override
	public boolean isReadable() {
		try {
			URL url = getURL();
			// 1.文件路径，由文件系统解析
			if (ResourceUtils.isFileURL(url)) {
				// Proceed with file system resolution
				File file = getFile();
				return (file.canRead() && !file.isDirectory());
			}
			else {
				// Try InputStream resolution for jar resources
				URLConnection con = url.openConnection();
				customizeConnection(con);
				if (con instanceof HttpURLConnection) {
					HttpURLConnection httpCon = (HttpURLConnection) con;
					int code = httpCon.getResponseCode();
					if (code != HttpURLConnection.HTTP_OK) {
						httpCon.disconnect();
						return false;
					}
				}
				long contentLength = con.getContentLengthLong();
				if (contentLength > 0) {
					return true;
				}
				else if (contentLength == 0) {
					// Empty file or directory -> not considered readable...
					return false;
				}
				else {
					// Fall back to stream existence: can we open the stream?
					getInputStream().close();
					return true;
				}
			}
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public boolean isFile() {
		try {
			URL url = getURL();
			if (url.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
				return VfsResourceDelegate.getResource(url).isFile();
			}
			return ResourceUtils.URL_PROTOCOL_FILE.equals(url.getProtocol());
		}
		catch (IOException ex) {
			return false;
		}
	}

	/**
	 * This implementation returns a File reference for the underlying class path
	 * resource, provided that it refers to a file in the file system.
	 * @see org.springframework.util.ResourceUtils#getFile(java.net.URL, String)
	 */
	// 获取文件对象
	@Override
	public File getFile() throws IOException {
		URL url = getURL();
		// url协议为"vfs"，交给VfsResourceDelegate处理
		if (url.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
			return VfsResourceDelegate.getResource(url).getFile();
		}
		// 其他则由ResourceUtils处理
		return ResourceUtils.getFile(url, getDescription());
	}

	/**
	 * This implementation determines the underlying File
	 * (or jar file, in case of a resource in a jar/zip).
	 */
	// 这个实现决定了底层文件(或jar文件，如果是JAR / zip中的资源)
	@Override
	protected File getFileForLastModifiedCheck() throws IOException {
		URL url = getURL();// 获取URL
		// 是否为jar
		if (ResourceUtils.isJarURL(url)) {
			// 提取jar的URL
			URL actualUrl = ResourceUtils.extractArchiveURL(url);
			if (actualUrl.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
				// url协议为"vfs"，交给VfsResourceDelegate处理
				return VfsResourceDelegate.getResource(actualUrl).getFile();
			}
			// 由ResourceUtils处理，获取File对象
			return ResourceUtils.getFile(actualUrl, "Jar URL");
		}
		else {
			return getFile();
		}
	}

	/**
	 * This implementation returns a File reference for the given URI-identified
	 * resource, provided that it refers to a file in the file system.
	 * @since 5.0
	 * @see #getFile(URI)
	 */
	protected boolean isFile(URI uri) {
		try {
			if (uri.getScheme().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
				return VfsResourceDelegate.getResource(uri).isFile();
			}
			return ResourceUtils.URL_PROTOCOL_FILE.equals(uri.getScheme());
		}
		catch (IOException ex) {
			return false;
		}
	}

	/**
	 * This implementation returns a File reference for the given URI-identified
	 * resource, provided that it refers to a file in the file system.
	 * @see org.springframework.util.ResourceUtils#getFile(java.net.URI, String)
	 */
	// 通过给定uri，获取File对象
	protected File getFile(URI uri) throws IOException {
		// url协议为"vfs"，交给VfsResourceDelegate处理
		if (uri.getScheme().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
			return VfsResourceDelegate.getResource(uri).getFile();
		}
		// 否则由ResourceUtils处理，获取File对象
		return ResourceUtils.getFile(uri, getDescription());
	}

	/**
	 * This implementation returns a FileChannel for the given URI-identified
	 * resource, provided that it refers to a file in the file system.
	 * @since 5.0
	 * @see #getFile()
	 */
	@Override
	public ReadableByteChannel readableChannel() throws IOException {
		try {
			// Try file system channel
			return FileChannel.open(getFile().toPath(), StandardOpenOption.READ);
		}
		catch (FileNotFoundException | NoSuchFileException ex) {
			// Fall back to InputStream adaptation in superclass
			return super.readableChannel();
		}
	}

	@Override
	public long contentLength() throws IOException {
		URL url = getURL();
		// 1.文件路径，由文件系统解析
		if (ResourceUtils.isFileURL(url)) {
			// Proceed with file system resolution
			File file = getFile();
			long length = file.length();
			if (length == 0L && !file.exists()) {
				throw new FileNotFoundException(getDescription() +
						" cannot be resolved in the file system for checking its content length");
			}
			return length;
		}
		else {
			// Try a URL connection content-length header
			// 2.否则，采用尝试获取URL链接请求中header的content-length属性
			URLConnection con = url.openConnection();
			customizeConnection(con);
			return con.getContentLengthLong();
		}
	}

	@Override
	public long lastModified() throws IOException {
		URL url = getURL();
		// 1.文件或jar，调用父类方法进行处理
		boolean fileCheck = false;
		if (ResourceUtils.isFileURL(url) || ResourceUtils.isJarURL(url)) {
			// Proceed with file system resolution
			fileCheck = true;
			try {
				File fileToCheck = getFileForLastModifiedCheck();
				long lastModified = fileToCheck.lastModified();
				if (lastModified > 0L || fileToCheck.exists()) {
					return lastModified;
				}
			}
			catch (FileNotFoundException ex) {
				// Defensively fall back to URL connection check instead
			}
		}
		// 2.否则，采用尝试获取URL链接请求中header的last-modified属性
		// Try a URL connection last-modified header
		URLConnection con = url.openConnection();
		customizeConnection(con);
		long lastModified = con.getLastModified();
		if (fileCheck && lastModified == 0 && con.getContentLengthLong() <= 0) {
			throw new FileNotFoundException(getDescription() +
					" cannot be resolved in the file system for checking its last-modified timestamp");
		}
		return lastModified;
	}

	/**
	 * Customize the given {@link URLConnection}, obtained in the course of an
	 * {@link #exists()}, {@link #contentLength()} or {@link #lastModified()} call.
	 * <p>Calls {@link ResourceUtils#useCachesIfNecessary(URLConnection)} and
	 * delegates to {@link #customizeConnection(HttpURLConnection)} if possible.
	 * Can be overridden in subclasses.
	 * @param con the URLConnection to customize
	 * @throws IOException if thrown from URLConnection methods
	 */
	// 自定义连接
	protected void customizeConnection(URLConnection con) throws IOException {
		ResourceUtils.useCachesIfNecessary(con);
		if (con instanceof HttpURLConnection) {
			customizeConnection((HttpURLConnection) con);
		}
	}

	/**
	 * Customize the given {@link HttpURLConnection}, obtained in the course of an
	 * {@link #exists()}, {@link #contentLength()} or {@link #lastModified()} call.
	 * <p>Sets request method "HEAD" by default. Can be overridden in subclasses.
	 * @param con the HttpURLConnection to customize
	 * @throws IOException if thrown from HttpURLConnection methods
	 */
	// 自定义连接
	protected void customizeConnection(HttpURLConnection con) throws IOException {
		// 设置method为HEAD，则表明只返回请求头部
		con.setRequestMethod("HEAD");
	}


	/**
	 * Inner delegate class, avoiding a hard JBoss VFS API dependency at runtime.
	 */
	// 内部委托类，在运行时避免硬JBoss VFS API的依赖
	private static class VfsResourceDelegate {

		// 委托给VfsResource处理
		public static Resource getResource(URL url) throws IOException {
			return new VfsResource(VfsUtils.getRoot(url));
		}

		public static Resource getResource(URI uri) throws IOException {
			return new VfsResource(VfsUtils.getRoot(uri));
		}
	}

}
