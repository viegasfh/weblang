# Web Language

Web Language was originally known as WebL, but due to trademark conflict, it was later changed to HP's Web Language. 

It was developed by Thomas Kistler and Hannes Marais at Compaq, and it was available until 2003.

Web Language is a programming language that was originally designed for Web document processing. It is a high level, object-oriented scripting language that incorporates two novel fatures: service combinators and a markup algebra. Service combinators are language constructs that provide reliable access to web services by mimicking a web surfer's behaviour when failure occurs while retrieving a page. The markup algebra extracts structured and unstructured values from pages for computation, and is based on algebraic operations on sets of markup elements. Web language is used to quickly build and experiment with custom web crawlers, meta-search engines, page transducers, shopping bots, etc. For those familiar with web crawling with Python using Scappy, URLLib, BeautifulSoup, then this is a specialized language that was implemented from the start to crawl the web and extract valuable data for analysis. It uses the World Wide Web as a huge database with data in formats such as HTML and XML.

## About this project

This project was created with the intention of bringing back to life this wonderful programming language that I came across back in 1999 through an article published in DR Dob's Journal. The project has been deleted from HP's website, but some of its information is still available via [way back machine](https://web.archive.org/web/20070507043202/http://www.hpl.hp.com/downloads/crl/webl/index.html). The project will be available through its original [license](License.md), which states that you can modify it and redistribute it, provided that you do not charge for it or do not include in a commercial project. 

## Milestones

The project was implemented in a very old version of Java, and as such it has a lot of code that was deprecated and no longer works with modern java runtime environments. To begin with, it will be ported to Java 8 and after it is all up and running, I will begin to draft enhancements to be added to the language so that it becomes usable as a language for web scraping by taking advantage of Java's runtime environment.


