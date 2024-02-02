<#-- @ftlvariable name="model" type="api.maven.plugin.angular.client.data.TypeScriptEnum" -->
/**
 * Autogenerated enum type representing ${model.className}
 */
export type ${model.name} = ${model.values?map(value -> "'" + value + "'")?join(" | ")};

/**
 * Autogenerated constant array containing all allowed enum values of ${model.name}
 */
export const ${model.name}s: ${model.name}[] = [${model.values?map(value -> "'" + value + "'")?join(", ")}];

/**
 * Autogenerated function for type checking
 *
 * @param {*} value the value to check
 * @returns {boolean} true if value is of type ${model.name}
 */
export function is${model.name}(value: unknown): value is ${model.name} {
    return typeof value === 'string' && ${model.name}s.includes(value as ${model.name});
}

/**
* Autogenerated function for casting
*
* @param {*} value the value to cast
* @returns {${model.name}} the value
*/
export function castTo${model.name}(value: unknown): ${model.name} {
    if (is${model.name}(value)) {
        return value;
    }

    throw new Error('CastError: Failed to cast value to a ${model.name}');
}