goog.provide('dommy.core');
goog.require('cljs.core');
goog.require('dommy.template');
goog.require('dommy.attrs');
goog.require('dommy.utils');
goog.require('clojure.string');
dommy.core.has_class_QMARK_ = dommy.attrs.has_class_QMARK_;
dommy.core.add_class_BANG_ = dommy.attrs.add_class_BANG_;
dommy.core.remove_class_BANG_ = dommy.attrs.remove_class_BANG_;
dommy.core.toggle_class_BANG_ = dommy.attrs.toggle_class_BANG_;
dommy.core.set_attr_BANG_ = dommy.attrs.set_attr_BANG_;
dommy.core.set_style_BANG_ = dommy.attrs.set_style_BANG_;
dommy.core.set_px_BANG_ = dommy.attrs.set_px_BANG_;
dommy.core.px = dommy.attrs.px;
dommy.core.style_str = dommy.attrs.style_str;
dommy.core.style = dommy.attrs.style;
dommy.core.remove_attr_BANG_ = dommy.attrs.remove_attr_BANG_;
dommy.core.attr = dommy.attrs.attr;
dommy.core.hidden_QMARK_ = dommy.attrs.hidden_QMARK_;
dommy.core.toggle_BANG_ = dommy.attrs.toggle_BANG_;
dommy.core.hide_BANG_ = dommy.attrs.hide_BANG_;
dommy.core.show_BANG_ = dommy.attrs.show_BANG_;
dommy.core.bounding_client_rect = dommy.attrs.bounding_client_rect;
dommy.core.dissoc_in = dommy.utils.dissoc_in;
dommy.core.__GT_Array = dommy.utils.__GT_Array;
dommy.core.set_html_BANG_ = (function set_html_BANG_(elem,html){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
elem__$1.innerHTML = html;
return elem__$1;
});
dommy.core.html = (function html(elem){
return dommy.template.__GT_node_like.call(null,elem).innerHTML;
});
dommy.core.set_text_BANG_ = (function set_text_BANG_(elem,text){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var prop = (cljs.core.truth_(elem__$1.textContent)?"textContent":"innerText");
(elem__$1[prop] = text);
return elem__$1;
});
dommy.core.text = (function text(elem){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var or__3824__auto__ = elem__$1.textContent;
if(cljs.core.truth_(or__3824__auto__))
{return or__3824__auto__;
} else
{return elem__$1.innerText;
}
});
dommy.core.value = (function value(elem){
return dommy.template.__GT_node_like.call(null,elem).value;
});
dommy.core.set_value_BANG_ = (function set_value_BANG_(elem,value){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
elem__$1.value = value;
return elem__$1;
});
/**
* append `child` to `parent`. 'parent' and 'child' should be node-like
* (work with dommy.template/->node-like). The node-like projection
* of parent is returned after appending child.
* @param {...*} var_args
*/
dommy.core.append_BANG_ = (function() {
var append_BANG_ = null;
var append_BANG___2 = (function (parent,child){
var G__4969 = dommy.template.__GT_node_like.call(null,parent);
G__4969.appendChild(dommy.template.__GT_node_like.call(null,child));
return G__4969;
});
var append_BANG___3 = (function() { 
var G__4971__delegate = function (parent,child,more_children){
var parent__$1 = dommy.template.__GT_node_like.call(null,parent);
var G__4970_4972 = cljs.core.seq.call(null,cljs.core.cons.call(null,child,more_children));
while(true){
if(G__4970_4972)
{var c_4973 = cljs.core.first.call(null,G__4970_4972);
append_BANG_.call(null,parent__$1,c_4973);
{
var G__4974 = cljs.core.next.call(null,G__4970_4972);
G__4970_4972 = G__4974;
continue;
}
} else
{}
break;
}
return parent__$1;
};
var G__4971 = function (parent,child,var_args){
var more_children = null;
if (goog.isDef(var_args)) {
  more_children = cljs.core.array_seq(Array.prototype.slice.call(arguments, 2),0);
} 
return G__4971__delegate.call(this, parent, child, more_children);
};
G__4971.cljs$lang$maxFixedArity = 2;
G__4971.cljs$lang$applyTo = (function (arglist__4975){
var parent = cljs.core.first(arglist__4975);
var child = cljs.core.first(cljs.core.next(arglist__4975));
var more_children = cljs.core.rest(cljs.core.next(arglist__4975));
return G__4971__delegate(parent, child, more_children);
});
G__4971.cljs$lang$arity$variadic = G__4971__delegate;
return G__4971;
})()
;
append_BANG_ = function(parent,child,var_args){
var more_children = var_args;
switch(arguments.length){
case 2:
return append_BANG___2.call(this,parent,child);
default:
return append_BANG___3.cljs$lang$arity$variadic(parent,child, cljs.core.array_seq(arguments, 2));
}
throw(new Error('Invalid arity: ' + arguments.length));
};
append_BANG_.cljs$lang$maxFixedArity = 2;
append_BANG_.cljs$lang$applyTo = append_BANG___3.cljs$lang$applyTo;
append_BANG_.cljs$lang$arity$2 = append_BANG___2;
append_BANG_.cljs$lang$arity$variadic = append_BANG___3.cljs$lang$arity$variadic;
return append_BANG_;
})()
;
/**
* prepend `child` to `parent`, both node-like
* return ->node-like projection of `parent`
* @param {...*} var_args
*/
dommy.core.prepend_BANG_ = (function() {
var prepend_BANG_ = null;
var prepend_BANG___2 = (function (parent,child){
var G__4978 = dommy.template.__GT_node_like.call(null,parent);
G__4978.insertBefore(dommy.template.__GT_node_like.call(null,child),parent.firstChild);
return G__4978;
});
var prepend_BANG___3 = (function() { 
var G__4980__delegate = function (parent,child,more_children){
var parent__$1 = dommy.template.__GT_node_like.call(null,parent);
var G__4979_4981 = cljs.core.seq.call(null,cljs.core.cons.call(null,child,more_children));
while(true){
if(G__4979_4981)
{var c_4982 = cljs.core.first.call(null,G__4979_4981);
prepend_BANG_.call(null,parent__$1,c_4982);
{
var G__4983 = cljs.core.next.call(null,G__4979_4981);
G__4979_4981 = G__4983;
continue;
}
} else
{}
break;
}
return parent__$1;
};
var G__4980 = function (parent,child,var_args){
var more_children = null;
if (goog.isDef(var_args)) {
  more_children = cljs.core.array_seq(Array.prototype.slice.call(arguments, 2),0);
} 
return G__4980__delegate.call(this, parent, child, more_children);
};
G__4980.cljs$lang$maxFixedArity = 2;
G__4980.cljs$lang$applyTo = (function (arglist__4984){
var parent = cljs.core.first(arglist__4984);
var child = cljs.core.first(cljs.core.next(arglist__4984));
var more_children = cljs.core.rest(cljs.core.next(arglist__4984));
return G__4980__delegate(parent, child, more_children);
});
G__4980.cljs$lang$arity$variadic = G__4980__delegate;
return G__4980;
})()
;
prepend_BANG_ = function(parent,child,var_args){
var more_children = var_args;
switch(arguments.length){
case 2:
return prepend_BANG___2.call(this,parent,child);
default:
return prepend_BANG___3.cljs$lang$arity$variadic(parent,child, cljs.core.array_seq(arguments, 2));
}
throw(new Error('Invalid arity: ' + arguments.length));
};
prepend_BANG_.cljs$lang$maxFixedArity = 2;
prepend_BANG_.cljs$lang$applyTo = prepend_BANG___3.cljs$lang$applyTo;
prepend_BANG_.cljs$lang$arity$2 = prepend_BANG___2;
prepend_BANG_.cljs$lang$arity$variadic = prepend_BANG___3.cljs$lang$arity$variadic;
return prepend_BANG_;
})()
;
/**
* insert `node` before `other`, both node-like,
* `other` must have a parent. return `node`
*/
dommy.core.insert_before_BANG_ = (function insert_before_BANG_(elem,other){
var actual_node = dommy.template.__GT_node_like.call(null,elem);
var other__$1 = dommy.template.__GT_node_like.call(null,other);
if(cljs.core.truth_(other__$1.parentNode))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'.-parentNode","\uFDD1'other"),cljs.core.hash_map("\uFDD0'line",102))))].join('')));
}
other__$1.parentNode.insertBefore(actual_node,other__$1);
return actual_node;
});
/**
* insert `node` after `other`, both node-like,
* `other` must have a parent. return `node`
*/
dommy.core.insert_after_BANG_ = (function insert_after_BANG_(elem,other){
var actual_node = dommy.template.__GT_node_like.call(null,elem);
var other__$1 = dommy.template.__GT_node_like.call(null,other);
var parent = other__$1.parentNode;
var temp__3971__auto___4985 = other__$1.nextSibling;
if(cljs.core.truth_(temp__3971__auto___4985))
{var next_4986 = temp__3971__auto___4985;
parent.insertBefore(actual_node,next_4986);
} else
{parent.appendChild(actual_node);
}
return actual_node;
});
/**
* replace `elem` with `new`, both node-like, return node-like projection of new.
* node-like projection of elem must have parent.
*/
dommy.core.replace_BANG_ = (function replace_BANG_(elem,new$){
var new$__$1 = dommy.template.__GT_node_like.call(null,new$);
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
if(cljs.core.truth_(elem__$1.parentNode))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'.-parentNode","\uFDD1'elem"),cljs.core.hash_map("\uFDD0'line",124))))].join('')));
}
elem__$1.parentNode.replaceChild(new$__$1,elem__$1);
return new$__$1;
});
dommy.core.replace_contents_BANG_ = (function replace_contents_BANG_(parent,node_like){
var G__4988 = dommy.template.__GT_node_like.call(null,parent);
G__4988.innerHTML = "";
dommy.core.append_BANG_.call(null,G__4988,node_like);
return G__4988;
});
/**
* remove node-like `elem` from parent, return node-like projection of elem
*/
dommy.core.remove_BANG_ = (function remove_BANG_(elem){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var G__4990 = elem__$1.parentNode;
G__4990.removeChild(elem__$1);
return G__4990;
});
dommy.core.selector = (function selector(data){
if(cljs.core.coll_QMARK_.call(null,data))
{return clojure.string.join.call(null," ",cljs.core.map.call(null,selector,data));
} else
{if((function (){var or__3824__auto__ = cljs.core.string_QMARK_.call(null,data);
if(or__3824__auto__)
{return or__3824__auto__;
} else
{return cljs.core.keyword_QMARK_.call(null,data);
}
})())
{return cljs.core.name.call(null,data);
} else
{return null;
}
}
});
/**
* a lazy seq of the ancestors of `node`
*/
dommy.core.ancestor_nodes = (function ancestor_nodes(elem){
return cljs.core.take_while.call(null,cljs.core.identity,cljs.core.iterate.call(null,(function (p1__4991_SHARP_){
return p1__4991_SHARP_.parentNode;
}),dommy.template.__GT_node_like.call(null,elem)));
});
/**
* returns a predicate on nodes that match `selector` at the
* time of this `matches-pred` call (may return outdated results
* if you fuck with the DOM)
*/
dommy.core.matches_pred = (function() {
var matches_pred = null;
var matches_pred__1 = (function (selector){
return matches_pred.call(null,document,selector);
});
var matches_pred__2 = (function (base,selector){
var matches = dommy.utils.__GT_Array.call(null,dommy.template.__GT_node_like.call(null,dommy.template.__GT_node_like.call(null,base)).querySelectorAll(dommy.core.selector.call(null,selector)));
return (function (elem){
return (matches.indexOf(elem) >= 0);
});
});
matches_pred = function(base,selector){
switch(arguments.length){
case 1:
return matches_pred__1.call(this,base);
case 2:
return matches_pred__2.call(this,base,selector);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
matches_pred.cljs$lang$arity$1 = matches_pred__1;
matches_pred.cljs$lang$arity$2 = matches_pred__2;
return matches_pred;
})()
;
/**
* closest ancestor of `node` (up to `base`, if provided)
* that matches `selector`
*/
dommy.core.closest = (function() {
var closest = null;
var closest__2 = (function (elem,selector){
return cljs.core.first.call(null,cljs.core.filter.call(null,dommy.core.matches_pred.call(null,selector),dommy.core.ancestor_nodes.call(null,dommy.template.__GT_node_like.call(null,elem))));
});
var closest__3 = (function (base,elem,selector){
var base__$1 = dommy.template.__GT_node_like.call(null,base);
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
return cljs.core.first.call(null,cljs.core.filter.call(null,dommy.core.matches_pred.call(null,base__$1,selector),cljs.core.take_while.call(null,(function (p1__4992_SHARP_){
return !((p1__4992_SHARP_ === base__$1));
}),dommy.core.ancestor_nodes.call(null,elem__$1))));
});
closest = function(base,elem,selector){
switch(arguments.length){
case 2:
return closest__2.call(this,base,elem);
case 3:
return closest__3.call(this,base,elem,selector);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
closest.cljs$lang$arity$2 = closest__2;
closest.cljs$lang$arity$3 = closest__3;
return closest;
})()
;
/**
* is `descendant` a descendant of `ancestor`?
*/
dommy.core.descendant_QMARK_ = (function descendant_QMARK_(descendant,ancestor){
var descendant__$1 = dommy.template.__GT_node_like.call(null,descendant);
var ancestor__$1 = dommy.template.__GT_node_like.call(null,ancestor);
if(cljs.core.truth_(ancestor__$1.contains))
{return ancestor__$1.contains(descendant__$1);
} else
{if(cljs.core.truth_(ancestor__$1.compareDocumentPosition))
{return ((ancestor__$1.compareDocumentPosition(descendant__$1) & (1 << 4)) != 0);
} else
{return null;
}
}
});
dommy.core.special_listener_makers = cljs.core.into.call(null,cljs.core.ObjMap.EMPTY,cljs.core.map.call(null,(function (p__4993){
var vec__4994 = p__4993;
var special_mouse_event = cljs.core.nth.call(null,vec__4994,0,null);
var real_mouse_event = cljs.core.nth.call(null,vec__4994,1,null);
return cljs.core.PersistentVector.fromArray([special_mouse_event,cljs.core.PersistentArrayMap.fromArrays([real_mouse_event],[(function (f){
return (function (event){
var related_target = event.relatedTarget;
var listener_target = (function (){var or__3824__auto__ = event.selectedTarget;
if(cljs.core.truth_(or__3824__auto__))
{return or__3824__auto__;
} else
{return event.currentTarget;
}
})();
if(cljs.core.truth_((function (){var and__3822__auto__ = related_target;
if(cljs.core.truth_(and__3822__auto__))
{return dommy.core.descendant_QMARK_.call(null,related_target,listener_target);
} else
{return and__3822__auto__;
}
})()))
{return null;
} else
{return f.call(null,event);
}
});
})])], true);
}),cljs.core.ObjMap.fromObject(["\uFDD0'mouseenter","\uFDD0'mouseleave"],{"\uFDD0'mouseenter":"\uFDD0'mouseover","\uFDD0'mouseleave":"\uFDD0'mouseout"})));
/**
* fires f if event.target is found with `selector`
*/
dommy.core.live_listener = (function live_listener(elem,selector,f){
return (function (event){
var temp__3974__auto__ = dommy.core.closest.call(null,dommy.template.__GT_node_like.call(null,elem),event.target,selector);
if(cljs.core.truth_(temp__3974__auto__))
{var selected_target = temp__3974__auto__;
event.selectedTarget = selected_target;
return f.call(null,event);
} else
{return null;
}
});
});
/**
* Returns a nested map of event listeners on `nodes`
*/
dommy.core.event_listeners = (function event_listeners(elem){
var or__3824__auto__ = dommy.template.__GT_node_like.call(null,elem).dommyEventListeners;
if(cljs.core.truth_(or__3824__auto__))
{return or__3824__auto__;
} else
{return cljs.core.ObjMap.EMPTY;
}
});
/**
* @param {...*} var_args
*/
dommy.core.update_event_listeners_BANG_ = (function() { 
var update_event_listeners_BANG___delegate = function (elem,f,args){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
return elem__$1.dommyEventListeners = cljs.core.apply.call(null,f,dommy.core.event_listeners.call(null,elem__$1),args);
};
var update_event_listeners_BANG_ = function (elem,f,var_args){
var args = null;
if (goog.isDef(var_args)) {
  args = cljs.core.array_seq(Array.prototype.slice.call(arguments, 2),0);
} 
return update_event_listeners_BANG___delegate.call(this, elem, f, args);
};
update_event_listeners_BANG_.cljs$lang$maxFixedArity = 2;
update_event_listeners_BANG_.cljs$lang$applyTo = (function (arglist__4996){
var elem = cljs.core.first(arglist__4996);
var f = cljs.core.first(cljs.core.next(arglist__4996));
var args = cljs.core.rest(cljs.core.next(arglist__4996));
return update_event_listeners_BANG___delegate(elem, f, args);
});
update_event_listeners_BANG_.cljs$lang$arity$variadic = update_event_listeners_BANG___delegate;
return update_event_listeners_BANG_;
})()
;
dommy.core.elem_and_selector = (function elem_and_selector(elem_sel){
if(cljs.core.sequential_QMARK_.call(null,elem_sel))
{return cljs.core.juxt.call(null,(function (p1__4995_SHARP_){
return dommy.template.__GT_node_like.call(null,cljs.core.first.call(null,p1__4995_SHARP_));
}),cljs.core.rest).call(null,elem_sel);
} else
{return cljs.core.PersistentVector.fromArray([dommy.template.__GT_node_like.call(null,elem_sel),null], true);
}
});
/**
* Adds `f` as a listener for events of type `event-type` on
* `elem-sel`, which must either be a DOM node, or a sequence
* whose first item is a DOM node.
* 
* In other words, the call to `listen!` can take two forms:
* 
* If `elem-sel` is a DOM node, i.e., you're doing something like:
* 
* (listen! elem :click click-handler)
* 
* then `click-handler` will be set as a listener for `click` events
* on the `elem`.
* 
* If `elem-sel` is a sequence:
* 
* (listen! [elem :.selector.for :.some.descendants] :click click-handler)
* 
* then `click-handler` will be set as a listener for `click` events
* on descendants of `elem` that match the selector
* 
* Also accepts any number of event-type and handler pairs for setting
* multiple listeners at once:
* 
* (listen! some-elem :click click-handler :hover hover-handler)
* @param {...*} var_args
*/
dommy.core.listen_BANG_ = (function() { 
var listen_BANG___delegate = function (elem_sel,type_fs){
if(cljs.core.even_QMARK_.call(null,cljs.core.count.call(null,type_fs)))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'even?",cljs.core.with_meta(cljs.core.list("\uFDD1'count","\uFDD1'type-fs"),cljs.core.hash_map("\uFDD0'line",256))),cljs.core.hash_map("\uFDD0'line",256))))].join('')));
}
var vec__5002_5007 = dommy.core.elem_and_selector.call(null,elem_sel);
var elem_5008 = cljs.core.nth.call(null,vec__5002_5007,0,null);
var selector_5009 = cljs.core.nth.call(null,vec__5002_5007,1,null);
var G__5003_5010 = cljs.core.seq.call(null,cljs.core.partition.call(null,2,type_fs));
while(true){
if(G__5003_5010)
{var vec__5005_5011 = cljs.core.first.call(null,G__5003_5010);
var orig_type_5012 = cljs.core.nth.call(null,vec__5005_5011,0,null);
var f_5013 = cljs.core.nth.call(null,vec__5005_5011,1,null);
var G__5004_5014 = cljs.core.seq.call(null,cljs.core._lookup.call(null,dommy.core.special_listener_makers,orig_type_5012,cljs.core.PersistentArrayMap.fromArrays([orig_type_5012],[cljs.core.identity])));
while(true){
if(G__5004_5014)
{var vec__5006_5015 = cljs.core.first.call(null,G__5004_5014);
var actual_type_5016 = cljs.core.nth.call(null,vec__5006_5015,0,null);
var factory_5017 = cljs.core.nth.call(null,vec__5006_5015,1,null);
var canonical_f_5018 = (cljs.core.truth_(selector_5009)?cljs.core.partial.call(null,dommy.core.live_listener,elem_5008,selector_5009):cljs.core.identity).call(null,factory_5017.call(null,f_5013));
dommy.core.update_event_listeners_BANG_.call(null,elem_5008,cljs.core.assoc_in,cljs.core.PersistentVector.fromArray([selector_5009,actual_type_5016,f_5013], true),canonical_f_5018);
if(cljs.core.truth_(elem_5008.addEventListener))
{elem_5008.addEventListener(cljs.core.name.call(null,actual_type_5016),canonical_f_5018);
} else
{elem_5008.attachEvent(cljs.core.name.call(null,actual_type_5016),canonical_f_5018);
}
{
var G__5019 = cljs.core.next.call(null,G__5004_5014);
G__5004_5014 = G__5019;
continue;
}
} else
{}
break;
}
{
var G__5020 = cljs.core.next.call(null,G__5003_5010);
G__5003_5010 = G__5020;
continue;
}
} else
{}
break;
}
return elem_sel;
};
var listen_BANG_ = function (elem_sel,var_args){
var type_fs = null;
if (goog.isDef(var_args)) {
  type_fs = cljs.core.array_seq(Array.prototype.slice.call(arguments, 1),0);
} 
return listen_BANG___delegate.call(this, elem_sel, type_fs);
};
listen_BANG_.cljs$lang$maxFixedArity = 1;
listen_BANG_.cljs$lang$applyTo = (function (arglist__5021){
var elem_sel = cljs.core.first(arglist__5021);
var type_fs = cljs.core.rest(arglist__5021);
return listen_BANG___delegate(elem_sel, type_fs);
});
listen_BANG_.cljs$lang$arity$variadic = listen_BANG___delegate;
return listen_BANG_;
})()
;
/**
* Removes event listener for the element defined in `elem-sel`,
* which is the same format as listen!.
* 
* The following forms are allowed, and will remove all handlers
* that match the parameters passed in:
* 
* (unlisten! [elem :.selector] :click event-listener)
* 
* (unlisten! [elem :.selector]
* :click event-listener
* :mouseover other-event-listener)
* @param {...*} var_args
*/
dommy.core.unlisten_BANG_ = (function() { 
var unlisten_BANG___delegate = function (elem_sel,type_fs){
if(cljs.core.even_QMARK_.call(null,cljs.core.count.call(null,type_fs)))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'even?",cljs.core.with_meta(cljs.core.list("\uFDD1'count","\uFDD1'type-fs"),cljs.core.hash_map("\uFDD0'line",285))),cljs.core.hash_map("\uFDD0'line",285))))].join('')));
}
var vec__5027_5032 = dommy.core.elem_and_selector.call(null,elem_sel);
var elem_5033 = cljs.core.nth.call(null,vec__5027_5032,0,null);
var selector_5034 = cljs.core.nth.call(null,vec__5027_5032,1,null);
var G__5028_5035 = cljs.core.seq.call(null,cljs.core.partition.call(null,2,type_fs));
while(true){
if(G__5028_5035)
{var vec__5030_5036 = cljs.core.first.call(null,G__5028_5035);
var orig_type_5037 = cljs.core.nth.call(null,vec__5030_5036,0,null);
var f_5038 = cljs.core.nth.call(null,vec__5030_5036,1,null);
var G__5029_5039 = cljs.core.seq.call(null,cljs.core._lookup.call(null,dommy.core.special_listener_makers,orig_type_5037,cljs.core.PersistentArrayMap.fromArrays([orig_type_5037],[cljs.core.identity])));
while(true){
if(G__5029_5039)
{var vec__5031_5040 = cljs.core.first.call(null,G__5029_5039);
var actual_type_5041 = cljs.core.nth.call(null,vec__5031_5040,0,null);
var __5042 = cljs.core.nth.call(null,vec__5031_5040,1,null);
var keys_5043 = cljs.core.PersistentVector.fromArray([selector_5034,actual_type_5041,f_5038], true);
var canonical_f_5044 = cljs.core.get_in.call(null,dommy.core.event_listeners.call(null,elem_5033),keys_5043);
dommy.core.update_event_listeners_BANG_.call(null,elem_5033,dommy.utils.dissoc_in,keys_5043);
if(cljs.core.truth_(elem_5033.removeEventListener))
{elem_5033.removeEventListener(cljs.core.name.call(null,actual_type_5041),canonical_f_5044);
} else
{elem_5033.detachEvent(cljs.core.name.call(null,actual_type_5041),canonical_f_5044);
}
{
var G__5045 = cljs.core.next.call(null,G__5029_5039);
G__5029_5039 = G__5045;
continue;
}
} else
{}
break;
}
{
var G__5046 = cljs.core.next.call(null,G__5028_5035);
G__5028_5035 = G__5046;
continue;
}
} else
{}
break;
}
return elem_sel;
};
var unlisten_BANG_ = function (elem_sel,var_args){
var type_fs = null;
if (goog.isDef(var_args)) {
  type_fs = cljs.core.array_seq(Array.prototype.slice.call(arguments, 1),0);
} 
return unlisten_BANG___delegate.call(this, elem_sel, type_fs);
};
unlisten_BANG_.cljs$lang$maxFixedArity = 1;
unlisten_BANG_.cljs$lang$applyTo = (function (arglist__5047){
var elem_sel = cljs.core.first(arglist__5047);
var type_fs = cljs.core.rest(arglist__5047);
return unlisten_BANG___delegate(elem_sel, type_fs);
});
unlisten_BANG_.cljs$lang$arity$variadic = unlisten_BANG___delegate;
return unlisten_BANG_;
})()
;
/**
* @param {...*} var_args
*/
dommy.core.listen_once_BANG_ = (function() { 
var listen_once_BANG___delegate = function (elem_sel,type_fs){
if(cljs.core.even_QMARK_.call(null,cljs.core.count.call(null,type_fs)))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'even?",cljs.core.with_meta(cljs.core.list("\uFDD1'count","\uFDD1'type-fs"),cljs.core.hash_map("\uFDD0'line",300))),cljs.core.hash_map("\uFDD0'line",300))))].join('')));
}
var vec__5051_5054 = dommy.core.elem_and_selector.call(null,elem_sel);
var elem_5055 = cljs.core.nth.call(null,vec__5051_5054,0,null);
var selector_5056 = cljs.core.nth.call(null,vec__5051_5054,1,null);
var G__5052_5057 = cljs.core.seq.call(null,cljs.core.partition.call(null,2,type_fs));
while(true){
if(G__5052_5057)
{var vec__5053_5058 = cljs.core.first.call(null,G__5052_5057);
var type_5059 = cljs.core.nth.call(null,vec__5053_5058,0,null);
var f_5060 = cljs.core.nth.call(null,vec__5053_5058,1,null);
dommy.core.listen_BANG_.call(null,elem_sel,type_5059,((function (G__5052_5057,vec__5053_5058,type_5059,f_5060){
return (function this_fn(e){
dommy.core.unlisten_BANG_.call(null,elem_sel,type_5059,this_fn);
return f_5060.call(null,e);
});})(G__5052_5057,vec__5053_5058,type_5059,f_5060))
);
{
var G__5061 = cljs.core.next.call(null,G__5052_5057);
G__5052_5057 = G__5061;
continue;
}
} else
{}
break;
}
return elem_sel;
};
var listen_once_BANG_ = function (elem_sel,var_args){
var type_fs = null;
if (goog.isDef(var_args)) {
  type_fs = cljs.core.array_seq(Array.prototype.slice.call(arguments, 1),0);
} 
return listen_once_BANG___delegate.call(this, elem_sel, type_fs);
};
listen_once_BANG_.cljs$lang$maxFixedArity = 1;
listen_once_BANG_.cljs$lang$applyTo = (function (arglist__5062){
var elem_sel = cljs.core.first(arglist__5062);
var type_fs = cljs.core.rest(arglist__5062);
return listen_once_BANG___delegate(elem_sel, type_fs);
});
listen_once_BANG_.cljs$lang$arity$variadic = listen_once_BANG___delegate;
return listen_once_BANG_;
})()
;
/**
* NOTE: ONLY TO BE USED FOR TESTS. May not work at mocking many
* event types or their bubbling behaviours
* 
* Creates an event of type `event-type`, optionally having
* `update-event!` mutate and return an updated event object,
* and fires it on `node`.
* Only works when `node` is in the DOM
* @param {...*} var_args
*/
dommy.core.fire_BANG_ = (function() { 
var fire_BANG___delegate = function (node,event_type,p__5063){
var vec__5065 = p__5063;
var update_event_BANG_ = cljs.core.nth.call(null,vec__5065,0,null);
if(dommy.core.descendant_QMARK_.call(null,node,document.documentElement))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'descendant?","\uFDD1'node","\uFDD1'js/document.documentElement"),cljs.core.hash_map("\uFDD0'line",319))))].join('')));
}
var update_event_BANG___$1 = (function (){var or__3824__auto__ = update_event_BANG_;
if(cljs.core.truth_(or__3824__auto__))
{return or__3824__auto__;
} else
{return cljs.core.identity;
}
})();
if(cljs.core.truth_(document.createEvent))
{var event = document.createEvent("Event");
event.initEvent(cljs.core.name.call(null,event_type),true,true);
return node.dispatchEvent(update_event_BANG___$1.call(null,event));
} else
{return node.fireEvent([cljs.core.str("on"),cljs.core.str(cljs.core.name.call(null,event_type))].join(''),update_event_BANG___$1.call(null,document.createEventObject()));
}
};
var fire_BANG_ = function (node,event_type,var_args){
var p__5063 = null;
if (goog.isDef(var_args)) {
  p__5063 = cljs.core.array_seq(Array.prototype.slice.call(arguments, 2),0);
} 
return fire_BANG___delegate.call(this, node, event_type, p__5063);
};
fire_BANG_.cljs$lang$maxFixedArity = 2;
fire_BANG_.cljs$lang$applyTo = (function (arglist__5066){
var node = cljs.core.first(arglist__5066);
var event_type = cljs.core.first(cljs.core.next(arglist__5066));
var p__5063 = cljs.core.rest(cljs.core.next(arglist__5066));
return fire_BANG___delegate(node, event_type, p__5063);
});
fire_BANG_.cljs$lang$arity$variadic = fire_BANG___delegate;
return fire_BANG_;
})()
;
