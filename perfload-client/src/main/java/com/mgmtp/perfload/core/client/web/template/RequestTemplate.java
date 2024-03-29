/*
 * Copyright (c) 2002-2015 mgm technology partners GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mgmtp.perfload.core.client.web.template;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.mgmtp.perfload.core.common.util.LtUtils.toDefaultString;

import java.util.Arrays;
import java.util.List;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Represents a pre-configured, possibly parameterized, request that is executed during the test
 * after resolving any parameters. A list of request templates make up a request flow.
 *
 * @author rnaegele
 */
@ThreadSafe
@Immutable
public final class RequestTemplate {

	private final String id;
	private final String type;
	private final String uri;
	private final String uriAlias;
	private final SetMultimap<String, String> requestParameters;
	private final SetMultimap<String, String> requestHeaders;
	private final List<HeaderExtraction> headerExtractions;
	private final List<DetailExtraction> detailExtractions;
	private Body body;

	private final String skip;
	private final String validateResponse;

	/**
	 * @param id
	 *            an optional id for the request
	 * @param type
	 *            the type of the request
	 * @param uri
	 *            the context-relativ URL (i. e. the pathinfo part of the URL) without the query
	 *            string
	 * @param uriAlias
	 *            an alias for the URI used for logging measurings
	 * @param requestParameters
	 *            a {@link SetMultimap} of request parameters
	 * @param detailExtractions
	 *            a map of details extractions
	 */
	public RequestTemplate(final String id, final String type, final String skip, final String uri,
			final String uriAlias,
			final SetMultimap<String, String> requestHeaders, final SetMultimap<String, String> requestParameters,
			final Body body, final List<HeaderExtraction> headerExtractions,
			final List<DetailExtraction> detailExtractions,
			final String validateResponse) {
		checkArgument(type != null, "Parameter 'type' must not be null.");
		checkArgument(uri != null, "Parameter 'uri' must not be null.");
		checkArgument(requestHeaders != null, "Parameter 'requestHeaders' must not be null.");
		checkArgument(requestParameters != null, "Parameter 'requestParameters' must not be null.");
		checkArgument(headerExtractions != null, "Parameter 'headerExtractions' must not be null.");
		checkArgument(detailExtractions != null, "Parameter 'detailExtractions' must not be null.");
		this.id = id;
		this.type = type;
		this.skip = skip;
		this.uri = uri;
		this.uriAlias = uriAlias;
		this.requestHeaders = ImmutableSetMultimap.copyOf(requestHeaders);
		this.requestParameters = ImmutableSetMultimap.copyOf(requestParameters);
		this.body = body;
		this.headerExtractions = ImmutableList.copyOf(headerExtractions);
		this.detailExtractions = ImmutableList.copyOf(detailExtractions);
		this.validateResponse = validateResponse;
	}

	public RequestTemplate(final String type, final String skip, final String uri,
						   final String uriAlias,
						   final SetMultimap<String, String> requestHeaders, final SetMultimap<String, String> requestParameters,
						   final Body body, final List<HeaderExtraction> headerExtractions,
						   final List<DetailExtraction> detailExtractions,
						   final String validateResponse) {
		this("",type,skip,uri,uriAlias,requestHeaders,requestParameters,body,headerExtractions,detailExtractions,validateResponse);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the skip
	 */
	public String getSkip() {
		return skip;
	}

	public boolean isSkipped() {
		return Boolean.parseBoolean(skip);
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the uriAlias
	 */
	public String getUriAlias() {
		return uriAlias;
	}

	/**
	 * @return the requestHeaders
	 */
	public SetMultimap<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	/**
	 * @return the requestParameters
	 */
	public SetMultimap<String, String> getRequestParameters() {
		return requestParameters;
	}

	/**
	 * @return the body
	 */
	public Body getBody() {
		return body;
	}
	
	/**
	 * 
	 * @param body
	 */
	public void setBody(Body body) {
		this.body = body;
	}

	/**
	 * @return the headerExtractions
	 */
	public List<HeaderExtraction> getHeaderExtractions() {
		return headerExtractions;
	}

	/**
	 * @return the detailExtractions
	 */
	public List<DetailExtraction> getDetailExtractions() {
		return detailExtractions;
	}

	/**
	 * @return the validateResponse
	 */
	public String getValidateResponse() {
		return validateResponse;
	}

	/**
	 * @return the validateResponse
	 */
	public boolean isValidateResponse() {
		return Boolean.parseBoolean(validateResponse);
	}

	@Override
	public String toString() {
		return toDefaultString(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (body == null ? 0 : body.hashCode());
		result = prime * result + (detailExtractions == null ? 0 : detailExtractions.hashCode());
		result = prime * result + (headerExtractions == null ? 0 : headerExtractions.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (requestHeaders == null ? 0 : requestHeaders.hashCode());
		result = prime * result + (requestParameters == null ? 0 : requestParameters.hashCode());
		result = prime * result + (skip == null ? 0 : skip.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		result = prime * result + (uri == null ? 0 : uri.hashCode());
		result = prime * result + (uriAlias == null ? 0 : uriAlias.hashCode());
		result = prime * result + (validateResponse == null ? 0 : validateResponse.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RequestTemplate other = (RequestTemplate) obj;
		if (body == null) {
			if (other.body != null) {
				return false;
			}
		} else if (!body.equals(other.body)) {
			return false;
		}
		if (detailExtractions == null) {
			if (other.detailExtractions != null) {
				return false;
			}
		} else if (!detailExtractions.equals(other.detailExtractions)) {
			return false;
		}
		if (headerExtractions == null) {
			if (other.headerExtractions != null) {
				return false;
			}
		} else if (!headerExtractions.equals(other.headerExtractions)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (requestHeaders == null) {
			if (other.requestHeaders != null) {
				return false;
			}
		} else if (!requestHeaders.equals(other.requestHeaders)) {
			return false;
		}
		if (requestParameters == null) {
			if (other.requestParameters != null) {
				return false;
			}
		} else if (!requestParameters.equals(other.requestParameters)) {
			return false;
		}
		if (skip == null) {
			if (other.skip != null) {
				return false;
			}
		} else if (!skip.equals(other.skip)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		if (uriAlias == null) {
			if (other.uriAlias != null) {
				return false;
			}
		} else if (!uriAlias.equals(other.uriAlias)) {
			return false;
		}
		if (validateResponse == null) {
			if (other.validateResponse != null) {
				return false;
			}
		} else if (!validateResponse.equals(other.validateResponse)) {
			return false;
		}
		return true;
	}

	/**
	 * Specifies a detail that is to be extracted from the response to this request.
	 */
	public static final class HeaderExtraction {
		private final String name;
		private final String placeholderName;

		/**
		 * @param name
		 *            the name of the header
		 * @param placeholderName
		 *            the name for the placeholder; if {@code null}, the name parameter is used
		 */
		public HeaderExtraction(final String name, final String placeholderName) {
			this.name = name;
			this.placeholderName = firstNonNull(placeholderName, name);
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the placeholderName
		 */
		public String getPlaceholderName() {
			return placeholderName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (name == null ? 0 : name.hashCode());
			result = prime * result + (placeholderName == null ? 0 : placeholderName.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			HeaderExtraction other = (HeaderExtraction) obj;
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (placeholderName == null) {
				if (other.placeholderName != null) {
					return false;
				}
			} else if (!placeholderName.equals(other.placeholderName)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return toDefaultString(this);
		}
	}

	/**
	 * Specifies a detail that is to be extracted from the response to this request.
	 */
	public static final class DetailExtraction {
		private final String name;
		private final String pattern;
		private final String groupIndexString;
		private final String defaultValue;
		private final String indexedString;
		private final String failIfNotFoundString;

		/**
		 * @param name
		 *            a name for this detail
		 * @param pattern
		 *            a regular expression used to find the detail in the response
		 * @param groupIndexString
		 *            the detail is extracted from the capturing group with this index
		 * @param defaultValue
		 *            default value used if the regular expression does not match
		 * @param failIfNotFoundString
		 *            specifies whether an exception should be thrown if extraction fails
		 */
		public DetailExtraction(final String name, final String pattern, final String groupIndexString,
				final String defaultValue, final String indexedString, final String failIfNotFoundString) {
			checkArgument(name != null, "Parameter 'name' must not be null.");
			checkArgument(pattern != null, "Parameter 'pattern' must not be null.");

			this.name = name;
			this.pattern = pattern;
			this.groupIndexString = groupIndexString;
			this.defaultValue = defaultValue;
			this.indexedString = indexedString;
			this.failIfNotFoundString = failIfNotFoundString;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the pattern
		 */
		public String getPattern() {
			return pattern;
		}

		/**
		 * @return the groupIndex
		 */
		public String getGroupIndexString() {
			return groupIndexString;
		}

		public int getGroupIndex() {
			return groupIndexString != null ? Integer.parseInt(groupIndexString) : 1;
		}

		/**
		 * @return the defaultValue
		 */
		public String getDefaultValue() {
			return defaultValue;
		}

		/**
		 * @return the indexed
		 */
		public String getIndexedString() {
			return indexedString;
		}

		public boolean isIndexed() {
			return indexedString != null && Boolean.parseBoolean(indexedString);
		}

		/**
		 * @return the failIfNotFound
		 */
		public String getFailIfNotFoundString() {
			return failIfNotFoundString;
		}

		public boolean isFailIfNotFound() {
			return failIfNotFoundString == null || Boolean.valueOf(failIfNotFoundString);
		}

		@Override
		public String toString() {
			return toDefaultString(this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (defaultValue == null ? 0 : defaultValue.hashCode());
			result = prime * result + (failIfNotFoundString == null ? 0 : failIfNotFoundString.hashCode());
			result = prime * result + (groupIndexString == null ? 0 : groupIndexString.hashCode());
			result = prime * result + (indexedString == null ? 0 : indexedString.hashCode());
			result = prime * result + (name == null ? 0 : name.hashCode());
			result = prime * result + (pattern == null ? 0 : pattern.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			DetailExtraction other = (DetailExtraction) obj;
			if (defaultValue == null) {
				if (other.defaultValue != null) {
					return false;
				}
			} else if (!defaultValue.equals(other.defaultValue)) {
				return false;
			}
			if (failIfNotFoundString == null) {
				if (other.failIfNotFoundString != null) {
					return false;
				}
			} else if (!failIfNotFoundString.equals(other.failIfNotFoundString)) {
				return false;
			}
			if (groupIndexString == null) {
				if (other.groupIndexString != null) {
					return false;
				}
			} else if (!groupIndexString.equals(other.groupIndexString)) {
				return false;
			}
			if (indexedString == null) {
				if (other.indexedString != null) {
					return false;
				}
			} else if (!indexedString.equals(other.indexedString)) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (pattern == null) {
				if (other.pattern != null) {
					return false;
				}
			} else if (!pattern.equals(other.pattern)) {
				return false;
			}
			return true;
		}
	}

	public static class Body {
		private final byte[] content;
		private final String resourcePath;
		private final String resourceType;

		/**
		 * Either content or resource path (and optionally charset) must be specified.
		 *
		 * @param content
		 *            the body content
		 * @param resourcePath
		 *            the path to the body resource
		 * @param resourceType
		 *            the type of the resource
		 */
		private Body(final byte[] content, final String resourcePath, final String resourceType) {
			checkState(content != null ^ (resourcePath != null && resourceType != null),
					"Must specify either body content or resource path and type.");
			this.content = content;
			this.resourcePath = resourcePath;
			this.resourceType = resourceType;
		}

		/**
		 * Creates a new Body instance from the specified content string. Uses UTF-8 as character
		 * set internally.
		 *
		 * @param content
		 *            the content string
		 * @return the Body object
		 */
		public static Body create(final String content) {
			return new Body(content.getBytes(Charsets.UTF_8), null, ResourceType.text.name());
		}

		/**
		 * Creates a new Body instance from the specified resource path and character set.
		 *
		 * @param resourcePath
		 *            the path of the classpath resource to load the body from
		 * @param resourceType
		 *            the type of the resource
		 * @return the Body object
		 */
		public static Body create(final String resourcePath, final String resourceType) {
			return new Body(null, resourcePath, resourceType);
		}

		/**
		 * Creates a new Body instance from the specified binary content.
		 * 
		 * Placeholders can be resolved in String content only.
		 *
		 * @param content
		 *            the body content
		 * @return the Body object
		 */
		public static Body create(final byte[] content) {			
			return new Body(content, null, ResourceType.binary.name());
		}

		/**
		 * @return the content
		 */
		public byte[] getContent() {
			return content;
		}

		/**
		 * @return the resourcePath
		 */
		public String getResourcePath() {
			return resourcePath;
		}

		/**
		 * @return the resourceType
		 */
		public String getResourceType() {
			return resourceType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(content);
			result = prime * result + (resourcePath == null ? 0 : resourcePath.hashCode());
			result = prime * result + (resourceType == null ? 0 : resourceType.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Body other = (Body) obj;
			if (!Arrays.equals(content, other.content)) {
				return false;
			}
			if (resourcePath == null) {
				if (other.resourcePath != null) {
					return false;
				}
			} else if (!resourcePath.equals(other.resourcePath)) {
				return false;
			}
			if (resourceType == null) {
				if (other.resourceType != null) {
					return false;
				}
			} else if (!resourceType.equals(other.resourceType)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return toDefaultString(this);
		}
	}
}
