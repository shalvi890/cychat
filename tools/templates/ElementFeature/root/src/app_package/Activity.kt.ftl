package ${escapeKotlinIdentifiers(packageName)}

import android.content.Context
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.addFragment
import com.cioinfotech.cychat.core.platform.ToolbarConfigurable
import com.cioinfotech.cychat.core.platform.VectorBaseActivity

//TODO: add this activity to manifest
class ${activityClass} : VectorBaseActivity(), ToolbarConfigurable {

    companion object {
	
		<#if createFragmentArgs>
		private const val EXTRA_FRAGMENT_ARGS = "EXTRA_FRAGMENT_ARGS"
		
		fun newIntent(context: Context, args: ${fragmentArgsClass}): Intent {
		     return Intent(context, ${activityClass}::class.java).apply {
		         putExtra(EXTRA_FRAGMENT_ARGS, args)
		      }
		}
		<#else>
        fun newIntent(context: Context): Intent {
            return Intent(context, ${activityClass}::class.java)
        }
		</#if>
    }

    override fun getLayoutRes() = R.layout.activity_simple

    override fun initUiAndData() {
        if (isFirstCreation()) {
			<#if createFragmentArgs>
			val fragmentArgs: ${fragmentArgsClass} = intent?.extras?.getParcelable(EXTRA_FRAGMENT_ARGS)
                                                   ?: return
            addFragment(R.id.simpleFragmentContainer, ${fragmentClass}::class.java, fragmentArgs)
			<#else>
			addFragment(R.id.simpleFragmentContainer, ${fragmentClass}::class.java)
			</#if>
        }
    }

    override fun configure(toolbar: Toolbar) {
        configureToolbar(toolbar)
    }

}
