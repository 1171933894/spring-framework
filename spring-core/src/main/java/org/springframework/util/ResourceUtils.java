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

package org.springframework.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.lang.Nullable;

/**
 * Utility methods for resolving resource locations to files in the
 * file system. Mainly for internal use within the framework.
 *
 * <p>Consider using Spring's Resource abstraction in the core package
 * for handling all kinds of file resources in a uniform manner.
 * {@link org.springframework.core.io.ResourceLoader}'s {@code getResource()}
 * method can resolve any location to a {@link org.springframework.core.io.Resource}
 * object, which in turn allows one to obtain a {@code java.io.File} in the
 * file system through its {@code getFile()} method.
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.FileSystemResource
 * @see org.springframework.core.io.UrlResource
 * @see org.springframework.core.io.ResourceLoader
 */
// 用途：用于将资源位置解析为文件系统中的文件的实用方法。主要用于Spring框架内的内部使用。
/**
 * 该工具被下面这些类都用到啦，可以说很重要
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.FileSystemResource
 * @see org.springframework.core.io.UrlResource
 * @see org.springframework.core.io.ResourceLoader
 **/
public abstract class ResourceUtils {

	// 加载类路径前缀："classpath:"
	/** Pseudo URL prefix for loading from the class path: "classpath:". */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	// 从文件系统加载的URL前缀"file:"
	/** URL prefix for loading from the file system: "file:". */
	public static final String FILE_URL_PREFIX = "file:";

	/** URL prefix for loading from a jar file: "jar:". */
	public static final String JAR_URL_PREFIX = "jar:";

	/** URL prefix for loading from a war file on Tomcat: "war:". */
	public static final String WAR_URL_PREFIX = "war:";

	/** URL protocol for a file in the file system: "file". */
	public static final String URL_PROTOCOL_FILE = "file";

	/** URL protocol for an entry from a jar file: "jar". */
	public static final String URL_PROTOCOL_JAR = "jar";

	/** URL protocol for an entry from a war file: "war". */
	public static final String URL_PROTOCOL_WAR = "war";

	/** URL protocol for an entry from a zip file: "zip". */
	public static final String URL_PROTOCOL_ZIP = "zip";

	/** URL protocol for an entry from a WebSphere jar file: "wsjar". */
	public static final String URL_PROTOCOL_WSJAR = "wsjar";

	/** URL protocol for an entry from a JBoss jar file: "vfszip". */
	public static final String URL_PROTOCOL_VFSZIP = "vfszip";

	/** URL protocol for a JBoss file system resource: "vfsfile". */
	public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

	/** URL protocol for a general JBoss VFS resource: "vfs". */
	public static final String URL_PROTOCOL_VFS = "vfs";

	/** File extension for a regular jar file: ".jar". */
	public static final String JAR_FILE_EXTENSION = ".jar";

	// jar中的JAR URL和文件路径之间的分隔符
	/** Separator between JAR URL and file path within the JAR: "!/". */
	public static final String JAR_URL_SEPARATOR = "!/";

	/** Special separator between WAR URL and jar part on Tomcat. */
	public static final String WAR_URL_SEPARATOR = "*/";


	/**
	 * Return whether the given resource location is a URL:
	 * either a special "classpath" pseudo URL or a standard URL.
	 * @param resourceLocation the location String to check
	 * @return whether the location qualifies as a URL
	 * @see #CLASSPATH_URL_PREFIX
	 * @see java.net.URL
	 */
	// 返回给定资源的位置是一个网址：无论是一个特殊的“路径”伪URL或一个标准的URL。
	public static boolean isUrl(@Nullable String resourceLocation) {
		if (resourceLocation == null) {
			return false;
		}
		// 特殊的“路径”伪URL:'classpath:'
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			return true;
		}
		try {
			// 一个标准的URL
			new URL(resourceLocation);
			return true;
		}
		catch (MalformedURLException ex) {
			return false;
		}
	}

	/**
	 * Resolve the given resource location to a {@code java.net.URL}.
	 * <p>Does not check whether the URL actually exists; simply returns
	 * the URL that the given location would correspond to.
	 * @param resourceLocation the resource location to resolve: either a
	 * "classpath:" pseudo URL, a "file:" URL, or a plain file path
	 * @return a corresponding URL object
	 * @throws FileNotFoundException if the resource cannot be resolved to a URL
	 */
	// 获取给定“资源定位”的URL
	public static URL getURL(String resourceLocation) throws FileNotFoundException {
		// 非空校验
		Assert.notNull(resourceLocation, "Resource location must not be null");
		// 特殊的“路径”伪URL:'classpath:'
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			// 截取classpath:后面的一段
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			// 获取当前类加载器
			ClassLoader cl = ClassUtils.getDefaultClassLoader();
			// 使用不同的类加载加载的根目录是不同的
			URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
			if (url == null) {
				String description = "class path resource [" + path + "]";
				throw new FileNotFoundException(description +
						" cannot be resolved to URL because it does not exist");
			}
			return url;
		}
		try {
			// 直接尝试是否为“标准URL”
			// try URL
			return new URL(resourceLocation);
		}
		catch (MalformedURLException ex) {
			// no URL -> treat as file path
			try {
				// 作为文件路径处理，再转换为URL
				return new File(resourceLocation).toURI().toURL();
			}
			catch (MalformedURLException ex2) {
				throw new FileNotFoundException("Resource location [" + resourceLocation +
						"] is neither a URL not a well-formed file path");
			}
		}
	}

	/**
	 * Resolve the given resource location to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * <p>Does not check whether the file actually exists; simply returns
	 * the File that the given location would correspond to.
	 * @param resourceLocation the resource location to resolve: either a
	 * "classpath:" pseudo URL, a "file:" URL, or a plain file path
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the resource cannot be resolved to
	 * a file in the file system
	 */
	// 获取给定“资源定位”的File
	public static File getFile(String resourceLocation) throws FileNotFoundException {
		// 非空校验
		Assert.notNull(resourceLocation, "Resource location must not be null");
		// 特殊的“路径”伪URL:'classpath:'
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			// 截取classpath:后面的一段
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			// 生成一段描述,用于抛错提示
			String description = "class path resource [" + path + "]";
			// 获取当前类加载器
			ClassLoader cl = ClassUtils.getDefaultClassLoader();
			URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
			if (url == null) {
				throw new FileNotFoundException(description +
						" cannot be resolved to absolute file path because it does not exist");
			}
			// 获取加载文件对象
			return getFile(url, description);
		}
		try {
			// try URL
			// 尝试利用URL获取File对象
			return getFile(new URL(resourceLocation));
		}
		catch (MalformedURLException ex) {
			// 不是标准Url，则作为文件路径处理
			// no URL -> treat as file path
			return new File(resourceLocation);
		}
	}

	/**
	 * Resolve the given resource URL to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * @param resourceUrl the resource URL to resolve
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 */
	// 利用URL获取File对象
	public static File getFile(URL resourceUrl) throws FileNotFoundException {
		return getFile(resourceUrl, "URL");
	}

	/**
	 * Resolve the given resource URL to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * @param resourceUrl the resource URL to resolve
	 * @param description a description of the original resource that
	 * the URL was created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 */
	// 尝试利用URL获取File对象
	public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
		Assert.notNull(resourceUrl, "Resource URL must not be null");
		if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
			throw new FileNotFoundException(
					description + " cannot be resolved to absolute file path " +
					"because it does not reside in the file system: " + resourceUrl);
		}
		try {
			return new File(toURI(resourceUrl).getSchemeSpecificPart());
		}
		catch (URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever happen).
			return new File(resourceUrl.getFile());
		}
	}

	/**
	 * Resolve the given resource URI to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * @param resourceUri the resource URI to resolve
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 * @since 2.5
	 */
	// 通过给定URI获取文件对象
	public static File getFile(URI resourceUri) throws FileNotFoundException {
		return getFile(resourceUri, "URI");
	}

	/**
	 * Resolve the given resource URI to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * @param resourceUri the resource URI to resolve
	 * @param description a description of the original resource that
	 * the URI was created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 * @since 2.5
	 */
	// 通过给定URI获取文件对象
	public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
		Assert.notNull(resourceUri, "Resource URI must not be null");
		if (!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
			throw new FileNotFoundException(
					description + " cannot be resolved to absolute file path " +
					"because it does not reside in the file system: " + resourceUri);
		}
		return new File(resourceUri.getSchemeSpecificPart());
	}

	/**
	 * Determine whether the given URL points to a resource in the file system,
	 * i.e. has protocol "file", "vfsfile" or "vfs".
	 * @param url the URL to check
	 * @return whether the URL has been identified as a file system URL
	 */
	// 判断给定的URL指向文件系统中。协议包含："file", "vfsfile" or "vfs".
	public static boolean isFileURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_FILE.equals(protocol) || URL_PROTOCOL_VFSFILE.equals(protocol) ||
				URL_PROTOCOL_VFS.equals(protocol));
	}

	/**
	 * Determine whether the given URL points to a resource in a jar file.
	 * i.e. has protocol "jar", "war, ""zip", "vfszip" or "wsjar".
	 * @param url the URL to check
	 * @return whether the URL has been identified as a JAR URL
	 */
	// 确定给定URL是否指向JAR文件中的资源。协议包含："jar", "zip", "vfszip", "wsjar" or "code-source".
	public static boolean isJarURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_WAR.equals(protocol) ||
				URL_PROTOCOL_ZIP.equals(protocol) || URL_PROTOCOL_VFSZIP.equals(protocol) ||
				URL_PROTOCOL_WSJAR.equals(protocol));
	}

	/**
	 * Determine whether the given URL points to a jar file itself,
	 * that is, has protocol "file" and ends with the ".jar" extension.
	 * @param url the URL to check
	 * @return whether the URL has been identified as a JAR file URL
	 * @since 4.1
	 */
	public static boolean isJarFileURL(URL url) {
		return (URL_PROTOCOL_FILE.equals(url.getProtocol()) &&
				url.getPath().toLowerCase().endsWith(JAR_FILE_EXTENSION));
	}

	/**
	 * Extract the URL for the actual jar file from the given URL
	 * (which may point to a resource in a jar file or to a jar file itself).
	 * @param jarUrl the original URL
	 * @return the URL for the actual jar file
	 * @throws MalformedURLException if no valid jar file URL could be extracted
	 */
	// 从给定URL中提取实际JAR文件的URL（它可以指向JAR文件中的资源或JAR文件本身）
	public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
		// 获取路径：‘jar:http://hostname/my.jar!/’
		String urlFile = jarUrl.getFile();
		int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
		if (separatorIndex != -1) {
			// 截取后为：‘jar:http://hostname/my.jar’
			String jarFile = urlFile.substring(0, separatorIndex);
			try {
				return new URL(jarFile);
			}
			catch (MalformedURLException ex) {
				// Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
				// 可能在原始JAR URL中没有协议, 例如：‘jar:C:/mypath/myjar.jar’.
				// This usually indicates that the jar file resides in the file system.
				// 这通常表示JAR文件在文件系统中
				if (!jarFile.startsWith("/")) {
					// 截取后：‘/mypath/myjar.jar’
					jarFile = "/" + jarFile;
				}
				// 标准URL加载：‘file:/mypath/myjar.jar’
				return new URL(FILE_URL_PREFIX + jarFile);
			}
		}
		else {
			return jarUrl;
		}
	}

	/**
	 * Extract the URL for the outermost archive from the given jar/war URL
	 * (which may point to a resource in a jar file or to a jar file itself).
	 * <p>In the case of a jar file nested within a war file, this will return
	 * a URL to the war file since that is the one resolvable in the file system.
	 * @param jarUrl the original URL
	 * @return the URL for the actual jar file
	 * @throws MalformedURLException if no valid jar file URL could be extracted
	 * @since 4.1.8
	 * @see #extractJarFileURL(URL)
	 */
	public static URL extractArchiveURL(URL jarUrl) throws MalformedURLException {
		String urlFile = jarUrl.getFile();

		int endIndex = urlFile.indexOf(WAR_URL_SEPARATOR);
		if (endIndex != -1) {
			// Tomcat's "war:file:...mywar.war*/WEB-INF/lib/myjar.jar!/myentry.txt"
			String warFile = urlFile.substring(0, endIndex);
			if (URL_PROTOCOL_WAR.equals(jarUrl.getProtocol())) {
				return new URL(warFile);
			}
			int startIndex = warFile.indexOf(WAR_URL_PREFIX);
			if (startIndex != -1) {
				return new URL(warFile.substring(startIndex + WAR_URL_PREFIX.length()));
			}
		}

		// Regular "jar:file:...myjar.jar!/myentry.txt"
		return extractJarFileURL(jarUrl);
	}

	/**
	 * Create a URI instance for the given URL,
	 * replacing spaces with "%20" URI encoding first.
	 * @param url the URL to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException if the URL wasn't a valid URI
	 * @see java.net.URL#toURI()
	 */
	public static URI toURI(URL url) throws URISyntaxException {
		/**
		 * 为什么会有URL转换为URI的逻辑？
		 * URI可以表示各种资源（包括Web上的，本地的等），而URL主要用于表示Web上的资源。http，ftp这种。
		 * File是被定义用来读取本地的资源文件，而不是读取Web上的资源文件。所以按设计来说，File的参数不能是URL。
		 */
		return toURI(url.toString());
	}

	/**
	 * Create a URI instance for the given location String,
	 * replacing spaces with "%20" URI encoding first.
	 * @param location the location String to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException if the location wasn't a valid URI
	 */
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(StringUtils.replace(location, " ", "%20"));
	}

	/**
	 * Set the {@link URLConnection#setUseCaches "useCaches"} flag on the
	 * given connection, preferring {@code false} but leaving the
	 * flag at {@code true} for JNLP based resources.
	 * @param con the URLConnection to set the flag on
	 */
	public static void useCachesIfNecessary(URLConnection con) {
		con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
	}

}
