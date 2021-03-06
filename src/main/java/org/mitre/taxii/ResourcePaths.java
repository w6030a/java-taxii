package org.mitre.taxii;

/*
Copyright (c) 2014, The MITRE Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The MITRE Corporation nor the 
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */

/**
 * Constants to locate the Schema and Schematron documents for the various versions of TAXII.
 * 
 * @author jasenj1
 */
public abstract class ResourcePaths {
    /** Path to the TAXII 1.0 XSD */
    public static final String TAXII_10_SCHEMA_RESOURCE = "/TAXII_XMLMessageBinding-1.0.xsd";
    /** Path to the XSLT containing the Schematron rules for TAXII 1.0 */
    public static final String TAXII_10_SCHEMATRON_XSLT_RESOURCE = "/TAXII_XMLMessageBinding-1.0-compiled.xsl";

    /** Path to the TAXII 1.1 XSD */
    public static final String TAXII_11_SCHEMA_RESOURCE = "/TAXII_XMLMessageBinding-1.1.xsd";
    /** Path to the XSLT containing the Schematron rules for TAXII 1.1 */
    public static final String TAXII_11_SCHEMATRON_XSLT_RESOURCE = "/TAXII_XMLMessageBinding-1.1-compiled.xsl";        
    
    private ResourcePaths() {}
}
