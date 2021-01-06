package com.automizely.framework.mvp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.koin.core.definition.BeanDefinition;
import org.koin.core.definition.Callbacks;
import org.koin.core.definition.Kind;
import org.koin.core.definition.Options;
import org.koin.core.definition.Properties;
import org.koin.core.module.Module;
import org.koin.core.parameter.DefinitionParameters;
import org.koin.core.qualifier.Qualifier;
import org.koin.core.scope.Scope;
import org.koin.core.scope.ScopeDefinition;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function2;
import kotlin.reflect.KClass;

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/6
 */
public final class ModuleJava {

    @NonNull
    public static <T> BeanDefinition<T> newBeanDefinition(
            @NonNull ScopeDefinition scopeDefinition,
            @NonNull Class<T> primaryType,
            @Nullable Qualifier qualifier,
            @NonNull Function2<Scope, DefinitionParameters, T> definition,
            @NonNull Kind kind,
            @Nullable List<KClass<?>> secondaryTypes,
            @Nullable Options options,
            @Nullable Properties properties,
            @Nullable Callbacks<T> callbacks
    ) {
        if (secondaryTypes == null) {
            secondaryTypes = new ArrayList<>();
        }
        if (options == null) {
            options = new Options();
        }
        if (properties == null) {
            properties = new Properties();
        }
        if (callbacks == null) {
            callbacks = new Callbacks<>();
        }
        return new BeanDefinition<T>(
                scopeDefinition,
                JvmClassMappingKt.getKotlinClass(primaryType),
                qualifier,
                definition,
                kind,
                secondaryTypes,
                options,
                properties,
                callbacks);
    }

    @NonNull
    public static <T> BeanDefinition<T> createSingle(
            @NonNull Class<T> primaryType,
            @Nullable Qualifier qualifier,
            @NonNull Function2<Scope, DefinitionParameters, T> definition,
            @NonNull ScopeDefinition scopeDefinition,
            @NonNull Options options,
            @Nullable List<KClass<?>> secondaryTypes
    ) {
        if (secondaryTypes == null) {
            secondaryTypes = new ArrayList<>();
        }
        return newBeanDefinition(
                scopeDefinition,
                primaryType,
                qualifier,
                definition,
                Kind.Single,
                secondaryTypes,
                options,
                null,
                null
        );
    }

    @NonNull
    public static <T> BeanDefinition<T> saveSingle(
            @NonNull Class<T> primaryType,
            @Nullable Qualifier qualifier,
            @NonNull Function2<Scope, DefinitionParameters, T> definition,
            @NonNull ScopeDefinition scopeDefinition,
            @NonNull Options options
    ) {
        BeanDefinition<T> beanDefinition = createSingle(primaryType, qualifier, definition, scopeDefinition, options, null);
        scopeDefinition.save(beanDefinition, false);
        return beanDefinition;
    }

    @NonNull
    public static <T> BeanDefinition<T> createFactory(
            @NonNull Class<T> primaryType,
            @Nullable Qualifier qualifier,
            @NonNull Function2<Scope, DefinitionParameters, T> definition,
            @NonNull ScopeDefinition scopeDefinition,
            @NonNull Options options,
            @Nullable List<KClass<?>> secondaryTypes
    ) {
        if (secondaryTypes == null) {
            secondaryTypes = new ArrayList<>();
        }
        return newBeanDefinition(
                scopeDefinition,
                primaryType,
                qualifier,
                definition,
                Kind.Factory,
                secondaryTypes,
                options,
                null,
                null
        );
    }

    @NonNull
    public static <T> BeanDefinition<T> saveFactory(
            @NonNull Class<T> primaryType,
            @Nullable Qualifier qualifier,
            @NonNull Function2<Scope, DefinitionParameters, T> definition,
            @NonNull ScopeDefinition scopeDefinition,
            @NonNull Options options
    ) {
        BeanDefinition<T> beanDefinition = createFactory(primaryType, qualifier, definition, scopeDefinition, options, null);
        scopeDefinition.save(beanDefinition, false);
        return beanDefinition;
    }

    @NonNull
    public static <T> BeanDefinition<T> single(
            @NonNull Module module,
            @NonNull Class<T> primaryType,
            @NonNull Function2<Scope, DefinitionParameters, T> definition
    ) {
        return single(module, primaryType, null, false, false, definition);
    }

    @NonNull
    public static <T> BeanDefinition<T> single(
            @NonNull Module module,
            @NonNull Class<T> primaryType,
            @Nullable Qualifier qualifier,
            boolean createdAtStart,
            boolean override,
            @NonNull Function2<Scope, DefinitionParameters, T> definition
    ) {
        return saveSingle(primaryType, qualifier, definition, module.getRootScope(), module.makeOptions(override, createdAtStart));
    }

    @NonNull
    public static <T> BeanDefinition<T> factory(
            @NonNull Module module,
            @NonNull Class<T> primaryType,
            @NonNull Function2<Scope, DefinitionParameters, T> definition
    ) {
        return factory(module, primaryType, null, false, definition);
    }

    @NonNull
    public static <T> BeanDefinition<T> factory(
            @NonNull Module module,
            @NonNull Class<T> primaryType,
            @Nullable Qualifier qualifier,
            boolean override,
            @NonNull Function2<Scope, DefinitionParameters, T> definition
    ) {
        return saveFactory(primaryType, qualifier, definition, module.getRootScope(), module.makeOptions(override, false));
    }

}
