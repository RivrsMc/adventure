/*
 * This file is part of adventure-text-minimessage, licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.text.minimessage;

import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import net.kyori.adventure.text.minimessage.transformation.TransformationRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * not public api.
 *
 * @since 4.0.0
 */
final class MiniMessageImpl implements MiniMessage {
  static final Consumer<List<String>> DEFAULT_ERROR_CONSUMER = message -> message.forEach(System.out::println);

  static final MiniMessage INSTANCE = new MiniMessageImpl(TransformationRegistry.standard(), TemplateResolver.empty(), false, null, DEFAULT_ERROR_CONSUMER);

  private final boolean strict;
  private final Appendable debugOutput;
  private final Consumer<List<String>> parsingErrorMessageConsumer;
  final MiniMessageParser parser;

  MiniMessageImpl(final @NotNull TransformationRegistry registry, final @NotNull TemplateResolver templateResolver, final boolean strict, final Appendable debugOutput, final @NotNull Consumer<List<String>> parsingErrorMessageConsumer) {
    this.parser = new MiniMessageParser(registry, templateResolver);
    this.strict = strict;
    this.debugOutput = debugOutput;
    this.parsingErrorMessageConsumer = parsingErrorMessageConsumer;
  }

  @Override
  public @NotNull Component deserialize(final @NotNull String input) {
    return this.parser.parseFormat(input, Context.of(this.strict, this.debugOutput, input, this));
  }

  @Override
  public @NotNull Component deserialize(final @NotNull String input, final @NotNull TemplateResolver templateResolver) {
    return this.parser.parseFormat(input, Context.of(this.strict, this.debugOutput, input, this, templateResolver));
  }

  @Override
  public @NotNull String serialize(final @NotNull Component component) {
    return MiniMessageSerializer.serialize(component);
  }

  @Override
  public @NotNull String escapeTokens(final @NotNull String input) {
    return this.parser.escapeTokens(input);
  }

  @Override
  public @NotNull String stripTokens(final @NotNull String input) {
    return this.parser.stripTokens(input);
  }

  /**
   * not public api.
   *
   * @return huhu.
   * @since 4.1.0
   */
  public @NotNull Consumer<List<String>> parsingErrorMessageConsumer() {
    return this.parsingErrorMessageConsumer;
  }

  @Override
  public @NotNull Builder toBuilder() {
    return new BuilderImpl(this);
  }

  static final class BuilderImpl implements Builder {
    private TransformationRegistry registry = TransformationRegistry.standard();
    private TemplateResolver templateResolver = null;
    private boolean strict = false;
    private Appendable debug = null;
    private Consumer<List<String>> parsingErrorMessageConsumer = DEFAULT_ERROR_CONSUMER;

    BuilderImpl() {
    }

    BuilderImpl(final MiniMessageImpl serializer) {
      this.registry = serializer.parser.registry;
      this.templateResolver = serializer.parser.templateResolver;
      this.strict = serializer.strict;
      this.debug = serializer.debugOutput;
      this.parsingErrorMessageConsumer = serializer.parsingErrorMessageConsumer;
    }

    @Override
    public @NotNull Builder transformations(final @NotNull TransformationRegistry transformationRegistry) {
      this.registry = requireNonNull(transformationRegistry, "transformationRegistry");
      return this;
    }

    @Override
    public @NotNull Builder transformations(final @NotNull Consumer<TransformationRegistry.Builder> modifier) {
      final TransformationRegistry.Builder builder = this.registry.toBuilder();
      modifier.accept(builder);
      this.registry = builder.build();
      return this;
    }

    @Override
    public @NotNull Builder templateResolver(final @Nullable TemplateResolver templateResolver) {
      this.templateResolver = templateResolver;
      return this;
    }

    @Override
    public @NotNull Builder strict(final boolean strict) {
      this.strict = strict;
      return this;
    }

    @Override
    public @NotNull Builder debug(final @Nullable Appendable debugOutput) {
      this.debug = debugOutput;
      return this;
    }

    @Override
    public @NotNull Builder parsingErrorMessageConsumer(final @NotNull Consumer<List<String>> consumer) {
      this.parsingErrorMessageConsumer = requireNonNull(consumer, "consumer");
      return this;
    }

    @Override
    public @NotNull MiniMessage build() {
      return new MiniMessageImpl(this.registry, this.templateResolver == null ? TemplateResolver.empty() : this.templateResolver, this.strict, this.debug, this.parsingErrorMessageConsumer);
    }
  }
}
