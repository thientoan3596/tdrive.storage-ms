package org.thluon.tdrive.dto;

/**
 * Represent entry of path
 * @param path path to file or directory
 * @param fileName name of file, null then the path is a directory
 */
public record PathEntry(String path,String fileName) {}