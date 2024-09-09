<#ftl output_format="JavaScript">
<#-- @ftlvariable name="imports" type="java.util.List<de.devx.project.commons.client.typescript.data.TypeScriptImportModel>" -->
<#-- @ftlvariable name="reduxThunkConfigIdentifier" type="java.lang.String" -->
<#-- @ftlvariable name="errorSerializerIdentifier" type="java.lang.String" -->
import { type AsyncThunkOptions } from '@reduxjs/toolkit';
<#list imports as import>
import { ${import.identifiers?join(", ")} } from '${import.path}';
</#list>

export const ThunkOptions: AsyncThunkOptions<unknown, ${reduxThunkConfigIdentifier}> = { ${errorSerializerIdentifier} };
export type State = ${reduxThunkConfigIdentifier}['state'];
export type Dispatch = ${reduxThunkConfigIdentifier}['dispatch'];
