package co.smartreceipts.android.identity.widget.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.identity.widget.login.LoginFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent(modules = { LoginModule.class })
public interface LoginFragmentSubcomponent extends AndroidInjector<LoginFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<LoginFragment> {

    }
}
