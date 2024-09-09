<#ftl output_format="JavaScript">
<#-- @ftlvariable name="services" type="java.util.List<de.devx.project.client.playwright.generator.data.PlaywrightServiceInfoModel>" -->
import { APIRequestContext } from '@playwright/test';
<#list services as service>
    import { ${service.type} } from '${service.path}';
</#list>

<#list services as service>
    export * from '${service.path}';
</#list>

export interface Services {
<#list services as service>
    ${service.name}: ${service.type};
</#list>
}

export function services(backendUrl: string): {[S in keyof Services]: (arg: { request: APIRequestContext }, use: (service: Services[S]) => Promise
<void>) => Promise
    <void>} {
        return {
        ${services?map(service -> service.name + ": ({ request }, use) => use(new " + service.type + "(request, backendUrl))")?join(',\n\t\t')}
        };
        }