package com.lotus.homeDashboard.common.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class RequestFilter implements Filter {

	private FilterConfig filterConfig = null;

	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
		final ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

		//inputstream caching
		requestWrapper.getInputStream().readAllBytes();
		
		chain.doFilter(requestWrapper, responseWrapper);
		responseWrapper.copyBodyToResponse();
	}

	public void init(FilterConfig filterConfiguration) {
		this.filterConfig = filterConfiguration;
	}

	public void destroy() {
		this.filterConfig = null;
	}
}
