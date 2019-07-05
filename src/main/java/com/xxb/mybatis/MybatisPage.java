package com.xxb.mybatis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

public class MybatisPage<T> implements Page<T>,Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PageQuerySuffix = "_list";
	
	private List<T> content = new ArrayList<T>();
	private Pageable pageable;
	private long total;
	
	public MybatisPage() {
	}
	
	public MybatisPage(Pageable pageable, int count, List<T> rows) {
		this.pageable = pageable;
		this.total = count;
		this.content = rows;
	}
	
	public void build(Pageable pageable, List<T> rows) {
		this.pageable = pageable;
		this.content = rows;
	}
	
	@Override
	public int getNumber() {
		return pageable == null ? 0 : pageable.getPageNumber();
	}

	@Override
	public int getSize() {
		return pageable == null ? 0 : pageable.getPageSize();
	}

	@Override
	public int getNumberOfElements() {
		return content.size();
	}

	@Override
	public List<T> getContent() {
		return this.content;
	}
	
	public void setContent(List<T> content) {
		this.content = content;
	}

	@Override
	public boolean hasContent() {
		if (content==null || content.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public Sort getSort() {
		return pageable == null ? null : pageable.getSort();
	}

	@Override
	public boolean isFirst() {
		return !hasPrevious();
	}

	@Override
	public boolean isLast() {
		return !hasNext();
	}

	@Override
	public boolean hasNext() {
		return getNumber() + 1 < getTotalPages();
	}

	@Override
	public boolean hasPrevious() {
		return getNumber() > 0;
	}

	@Override
	public Pageable nextPageable() {
		return hasNext() ? pageable.next() : null;
	}

	@Override
	public Pageable previousPageable() {
		if (hasPrevious()) {
			return pageable.previousOrFirst();
		}

		return null;
	}

	@Override
	public int getTotalPages() {
		return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
	}

	@Override
	public long getTotalElements() {
		return total;
	}
	
	@Override
	public <U> Page<U> map(Function<? super T, ? extends U> converter) {
		return new PageImpl<>(getConvertedContent(converter), getPageable(), total);
	}

	public void setPageable(Pageable pageable) {
		this.pageable = pageable;
	}
	
	@Override
	public Iterator<T> iterator() {
		return this.content.iterator();
	}
	
	protected <U> List<U> getConvertedContent(Function<? super T, ? extends U> converter) {

		Assert.notNull(converter, "Function must not be null!");

		return this.stream().map(converter::apply).collect(Collectors.toList());
	}
}
